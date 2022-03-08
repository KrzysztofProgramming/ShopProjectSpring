package me.practice.shop.shop.controllers.products;

import me.practice.shop.shop.controllers.authors.AuthorsManager;
import me.practice.shop.shop.controllers.products.events.BeforeProductCreateEvent;
import me.practice.shop.shop.controllers.products.events.BeforeProductDeleteEvent;
import me.practice.shop.shop.controllers.products.events.BeforeProductUpdateEvent;
import me.practice.shop.shop.controllers.products.models.*;
import me.practice.shop.shop.database.files.DatabaseImage;
import me.practice.shop.shop.database.products.CommonTypesRepository;
import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.models.Author;
import me.practice.shop.shop.models.BookProduct;
import me.practice.shop.shop.models.ErrorResponse;
import me.practice.shop.shop.models.GetByParamsResponse;
import me.practice.shop.shop.utils.IterableUtils;
import me.practice.shop.shop.utils.MediaTypeUtils;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RequestMapping(value = "api/products/")
public class ProductsController {

    private final ErrorResponse productNotExistsInfo = new ErrorResponse(
            "Taki produkt nie istnieje lub już został usunięty");

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private ProductsImagesRepository productsImagesRepository;

    @Autowired
    private AuthorsManager authorsManager;

    @Autowired
    private TypesManager typesManager;

    @Autowired
    private CommonTypesRepository typesRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @GetMapping(value = "getAll")
    public ResponseEntity<?> getProducts(@Valid GetProductsParams params) {
        Page<BookProduct> productPage = this.productsRepository.findByParams(params);
        Page<ProductResponse> responsePage = productPage.map(ProductResponse::new);
        return ResponseEntity.ok(new GetByParamsResponse<>(productPage.getNumber() + 1, productPage.getTotalPages(),
                productPage.getTotalElements(), responsePage.toList()));
    }


    @GetMapping(value = "byId/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {
        Optional<BookProduct> product = productsRepository.findById(id);
        return product.isPresent() ? ResponseEntity.ok(product.map(ProductResponse::new))
                : ResponseEntity.badRequest().body(productNotExistsInfo);
    }

    @GetMapping(value = "byIds")
    public ResponseEntity<?> getProductsByIds(@RequestParam @Valid @NotEmpty List<String> ids) {
        Iterable<BookProduct> products = productsRepository.findAllById(ids);
        return ResponseEntity.ok(StreamSupport.stream(products.spliterator(), false)
                .map(ProductResponse::new).collect(Collectors.toList()));
    }

    @GetMapping(value = "getTypes")
    public ResponseEntity<?> getTypes(){
        return ResponseEntity.ok(new TypesResponse(IterableUtils.toList(this.typesManager.getTypesAsStrings())));
    }

    @GetMapping(value = "getTypesWithCount")
    public ResponseEntity<?> getTypesWithCount(){
        return ResponseEntity.ok(this.typesRepository.findAll());
    }

    @PreAuthorize("hasAuthority('products:write')")
    @PostMapping(value = "newType")
    public ResponseEntity<?> createNewType(@Valid @RequestBody TypeRequest request){
        return this.typesManager.addNewType(request.getName());
    }

    @PreAuthorize("hasAuthority('products:write')")
    @PostMapping(value = "newProduct")
    public ResponseEntity<?> addProduct(@Valid @RequestBody ProductRequest request) {
        return this.validateProductRequest(request, (authors, types)->{
            this.eventPublisher.publishEvent(new BeforeProductCreateEvent(this, request));
            return ResponseEntity.ok().body(new ProductResponse(
            productsRepository.insert(this.newProduct(request, authors, types))));
        });
    }

    @PreAuthorize("hasAuthority('products:write')")
    @PutMapping(value="recalcTypesCounters")
    public ResponseEntity<?> recalcTypesCounters(){
        return ResponseEntity.ok().body(this.typesManager.recalcTypesCounters());
    }

    @PreAuthorize("hasAuthority('products:write')")
    @PutMapping(value = "updateProduct/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable String id, @Valid @RequestBody ProductRequest request) {
        return this.validateProductRequest(request, (authors, types)-> productsRepository.findById(id)
                .map(product -> {
                    this.eventPublisher.publishEvent(new BeforeProductUpdateEvent(this, request, product));
                    return ResponseEntity.ok((Object) new ProductResponse(
                            productsRepository.save(this.fromRequest(product.getId(), request, authors, types))));
                }).orElse(ResponseEntity.badRequest().body(productNotExistsInfo)));
    }

    private ResponseEntity<?> validateProductRequest(ProductRequest request,
                                                     BiFunction<Collection<Author>, Collection<String>, ResponseEntity<?>> fn){
        Collection<Author> authors = this.authorsManager.getAuthorsByNames(request.getAuthorsNames());
        if (authors.size() != request.getAuthorsNames().size())
            return ResponseEntity.badRequest().body(new ErrorResponse("Zły autor/autorzy"));
        Collection<String> types = this.typesManager.getTypesByNames(request.getTypes());
        if (types.size() != request.getTypes().size())
            return ResponseEntity.badRequest().body(new ErrorResponse("Zły typ/typy"));
        return fn.apply(authors, types);
    }

    @PreAuthorize("hasAuthority('products:write')")
    @DeleteMapping(value = "deleteProduct/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable String id) {
        BookProduct removingProduct = this.productsRepository.findById(id).orElse(null);
        if(removingProduct==null) return ResponseEntity.ok().build();
        this.eventPublisher.publishEvent(new BeforeProductDeleteEvent(this, removingProduct));
        productsRepository.deleteById(id);
        this.productsImagesRepository.deleteProductImages(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('products:write')")
    @PutMapping("uploadProductImage/{id}")
    public ResponseEntity<?> uploadProductImage(@PathVariable("id") String productId,
                                                @RequestParam("file") MultipartFile file) throws IOException {
        if (!productsRepository.existsById(productId)) {
            return ResponseEntity.badRequest().body(this.productNotExistsInfo);
        }
        String fileType = MediaTypeUtils.detectFileType(file.getBytes());
        if (!MediaTypeUtils.isImageTypeOK(fileType)) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Zły typ pliku"));
        }
        productsImagesRepository.saveAndScale(new DatabaseImage(productId, fileType, new Binary(file.getBytes())));
        return ResponseEntity.ok().build();
    }


    @PreAuthorize("hasAuthority('products:write')")
    @DeleteMapping("deleteProductImage/{id}")
    public ResponseEntity<?> deleteProductImage(@PathVariable("id") String productId) {
        this.productsImagesRepository.deleteProductImages(productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("downloadProductOriginalImage/{id}")
    public ResponseEntity<?> downloadOriginalProductImage(@PathVariable("id") String productId) {
        return this.downloadProductImage(this.productsImagesRepository.getOriginalImage(productId));
    }

    @GetMapping("downloadProductSmallImage/{id}")
    public ResponseEntity<?> downloadSmallProductImage(@PathVariable("id") String productId) {
        return this.downloadProductImage(this.productsImagesRepository.getSmallImage(productId));
    }

    @GetMapping("downloadProductIcon/{id}")
    public ResponseEntity<?> downloadProductIcon(@PathVariable("id") String productId){
        return this.downloadProductImage(this.productsImagesRepository.getIcon(productId));
    }

    private ResponseEntity<?> downloadProductImage(Optional<DatabaseImage> image) {
        if (image.isEmpty())
            return ResponseEntity.badRequest().body(new ErrorResponse("Ten produkt nie ma obrazu"));
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(image.get().getMediaType()))
                .body(image.get().getImage().getData());
    }

    private BookProduct newProduct(ProductRequest request, Collection<Author> authors, Collection<String> types) {
        return fromRequest(UUID.randomUUID().toString(), request, authors, types);
    }

    private BookProduct fromRequest(String id, ProductRequest request, Collection<Author> authors, Collection<String> types) {
        return new BookProduct(id, request.getName(), Math.floor(request.getPrice() * 100) / 100,
                request.getDescription(), authors,
                authors.stream().map(Author::getName).collect(Collectors.toList()),
                types, request.getInStock());
    }
}
