package me.practice.shop.shop.controllers.products;

import me.practice.shop.shop.controllers.authors.AuthorsManager;
import me.practice.shop.shop.controllers.products.models.*;
import me.practice.shop.shop.database.files.DatabaseImage;
import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.database.products.types.CommonTypesRepository;
import me.practice.shop.shop.models.*;
import me.practice.shop.shop.utils.MediaTypeUtils;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
        Page<BookProduct> productPage =this.productsRepository.findAll(PageRequest.of(params.getPageNumber(),
                params.getPageSize())); // this.productsRepository.findByParams(params); TODO
        Page<ProductResponse> responsePage = productPage.map(ProductResponse::new);
        return ResponseEntity.ok(new GetByParamsResponse<>(productPage.getNumber() + 1, productPage.getTotalPages(),
                productPage.getTotalElements(), responsePage.toList()));
    }


    @GetMapping(value = "byId/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        Optional<BookProduct> product = productsRepository.findById(id);
        return product.isPresent() ? ResponseEntity.ok(product.map(ProductResponse::new))
                : ResponseEntity.badRequest().body(productNotExistsInfo);
    }

    @GetMapping(value = "byIds")
    public ResponseEntity<?> getProductsByIds(@RequestParam @Valid @NotEmpty List<Long> ids) {
        Iterable<BookProduct> products = productsRepository.findAllById(ids);
        return ResponseEntity.ok(StreamSupport.stream(products.spliterator(), false)
                .map(ProductResponse::new).collect(Collectors.toList()));
    }

    @GetMapping(value = "getTypes")
    public ResponseEntity<?> getTypes(){
        return ResponseEntity.ok(new TypesResponse(this.typesManager.getTypesAsStrings()));
    }

    @PreAuthorize("hasAuthority('products:write')")
    @GetMapping(value="getTypesDetails")
    public ResponseEntity<?> getTypesDetails(@Valid GetTypesParams params){
        Page<CommonType> result = this.typesRepository.findAll(PageRequest.of(params.getPageNumber(),
                params.getPageSize())); //findByParams(params); TODO
        return ResponseEntity.ok(new GetByParamsResponse<>(result.getNumber() + 1,
                result.getTotalPages(), result.getTotalElements(), result.getContent()));
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
    @PutMapping(value = "updateType/{name}")
    public ResponseEntity<?> updateType(@PathVariable String name, @RequestBody @Valid TypeRequest request){
        return this.typesManager.updateType(name, request.getName());
    }

    @PreAuthorize("hasAuthority('products:write')")
    @DeleteMapping(value = "deleteType/{name}")
    public ResponseEntity<?> deleteType(@PathVariable String name){
        this.typesRepository.deleteByName(name);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('products:write')")
    @PostMapping(value = "newProduct")
    public ResponseEntity<?> addProduct(@Valid @RequestBody ProductRequest request) {
        return this.validateProductRequest(request, (authors, types)->{
            return ResponseEntity.ok().body(new ProductResponse(
            this.productsRepository.save(this.newProduct(request, authors, types))));
        });
    }

    @PreAuthorize("hasAuthority('products:write')")
    @PutMapping(value = "updateProduct/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return this.validateProductRequest(request, (authors, types)-> productsRepository.findById(id)
                .map(product -> {
                    return ResponseEntity.ok((Object) new ProductResponse(
                            productsRepository.save(this.fromRequest(product.getId(), request, authors, types))));
                }).orElse(ResponseEntity.badRequest().body(productNotExistsInfo)));
    }

    private ResponseEntity<?> validateProductRequest(ProductRequest request,
                                                     BiFunction<Set<Author>, Set<String>, ResponseEntity<?>> fn){
        Set<Author> authors = this.authorsManager.getAuthorsByNames(request.getAuthorsNames());
        if (authors.size() != request.getAuthorsNames().size())
            return ResponseEntity.badRequest().body(new ErrorResponse("Zły autor/autorzy"));
        Set<String> types = this.typesManager.getTypesByNames(request.getTypes());
        if (types.size() != request.getTypes().size())
            return ResponseEntity.badRequest().body(new ErrorResponse("Zły typ/typy"));
        return fn.apply(authors, types);
    }

    @PreAuthorize("hasAuthority('products:write')")
    @DeleteMapping(value = "deleteProduct/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        BookProduct removingProduct = this.productsRepository.findById(id).orElse(null);
        if(removingProduct==null) return ResponseEntity.ok().build();
        productsRepository.deleteById(id);
        this.productsImagesRepository.deleteProductImages(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('products:write')")
    @PutMapping("uploadProductImage/{id}")
    public ResponseEntity<?> uploadProductImage(@PathVariable("id") Long productId,
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
    public ResponseEntity<?> deleteProductImage(@PathVariable("id") Long productId) {
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

    private BookProduct newProduct(ProductRequest request, Set<Author> authors, Set<String> types) {
        return BookProduct.builder()
                .name(request.getName())
                .price(Math.floor(request.getPrice() * 100) / 100)
                .description(request.getDescription())
                .authors(authors)
                .types(types.stream().map(CommonType::new).collect(Collectors.toSet()))
                .inStock(request.getInStock()).build();
    }

    private BookProduct fromRequest(Long id, ProductRequest request, Set<Author> authors, Set<String> types) {
        BookProduct bp = this.newProduct(request, authors, types);
        bp.setId(id);
        return bp;
    }
}
