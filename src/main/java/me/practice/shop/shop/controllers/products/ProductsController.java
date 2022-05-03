package me.practice.shop.shop.controllers.products;

import me.practice.shop.shop.controllers.products.models.*;
import me.practice.shop.shop.database.files.DatabaseImage;
import me.practice.shop.shop.database.files.ImageIdentifier;
import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.database.products.ProductsSearcher;
import me.practice.shop.shop.database.products.types.CommonTypesRepository;
import me.practice.shop.shop.models.*;
import me.practice.shop.shop.permissions.Permissions;
import me.practice.shop.shop.services.FunctionsService;
import me.practice.shop.shop.utils.MediaTypeUtils;
import org.hibernate.TransientObjectException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    private ProductsImagesManager productsImagesManager;

    @Autowired
    private TypesManager typesManager;

    @Autowired
    private CommonTypesRepository typesRepository;

    @Autowired
    private FunctionsService functionsService;

    @Autowired
    private ProductsSearcher productsSearcher;

    @GetMapping(value = "getAll")
    public ResponseEntity<?> getProducts(@Valid GetProductsParams params) {
//        return ResponseEntity.ok(this.productsRepository.findAll());
        Page<BookProduct> productPage = this.productsSearcher.findByParams(params);
        Page<ProductResponse> responsePage = productPage.map(ProductResponse::new);
        return ResponseEntity.ok(new GetByParamsResponse<>(productPage.getNumber() + 1, productPage.getTotalPages(),
                productPage.getTotalElements(), responsePage.toList()));
    }


    @GetMapping(value = "byId/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        Optional<ShopUser> user = this.functionsService.getUserIfLoggedIn();
        boolean addDeletableFlag = user.isPresent() && Permissions.hasAllOf(user.get().getAuthoritiesNumber(),
                Permissions.PRODUCTS_WRITE.getNumberValue());
        Optional<BookProduct> product = productsRepository.findById(id);
        return product.isPresent() ? ResponseEntity.ok(addDeletableFlag ?
                new ProductResponse(product.get(), this.productsRepository) : new ProductResponse(product.get()))
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
        return ResponseEntity.ok(new TypesResponse(this.typesRepository.findAll().stream()
                .map(TypeResponse::new).collect(Collectors.toSet())));
    }

    @PreAuthorize("hasAuthority('products:write')")
    @GetMapping(value="getTypesDetails")
    public ResponseEntity<?> getTypesDetails(@Valid GetTypesParams params){
        Page<TypeDetailsResponse> result = this.typesManager.findTypeResponsesByParams(params);
        return ResponseEntity.ok(new GetByParamsResponse<>(result.getNumber() + 1,
                result.getTotalPages(), result.getTotalElements(), result.getContent()));
    }

//    @GetMapping(value = "getTypesWithCount")
//    public ResponseEntity<?> getTypesWithCount(){
//        return ResponseEntity.ok(this.typesRepository.findAll());
//    }

    @PreAuthorize("hasAuthority('products:write')")
    @PostMapping(value = "newType")
    public ResponseEntity<?> createNewType(@Valid @RequestBody TypeRequest request){
        return this.typesManager.addNewType(request.getName());
    }

    @PreAuthorize("hasAuthority('products:write')")
    @PutMapping(value = "updateType/{id}")
    public ResponseEntity<?> updateType(@PathVariable Long id, @RequestBody @Valid TypeRequest request){
        return this.typesManager.updateType(id, request.getName());
    }

    @PreAuthorize("hasAuthority('products:write')")
    @DeleteMapping(value = "deleteType/{id}")
    public ResponseEntity<?> deleteType(@PathVariable Long id){
        this.typesRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('products:write')")
    @PostMapping(value = "newProduct")
    public ResponseEntity<?> addProduct(@Valid @RequestBody ProductRequest request) {
        try{
            return ResponseEntity.ok().body(new ProductResponse(
                    this.productsRepository.save(this.newProduct(request))
            ));
        }
        catch(TransientObjectException e){
            return ResponseEntity.badRequest().body(new ErrorResponse("Błędni autorzy lub typy"));
        }
    }

    @PreAuthorize("hasAuthority('products:write')")
    @PutMapping(value = "archiveProduct/{id}")
    public ResponseEntity<?> archiveProduct(@PathVariable Long id, @Valid @RequestBody ArchiveRequest request){
        return this.productsRepository.archiveProduct(id, request.getArchive()) > 0 ?
                ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @PreAuthorize("hasAuthority('products:write')")
    @PutMapping(value = "updateProduct/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        Optional<BookProduct> product = this.productsRepository.findById(id);
        if(product.isEmpty())
            return ResponseEntity.badRequest().body(new ErrorResponse("Brak produktu o podanym id"));

        BookProduct changedProduct = this.fromRequest(id, request);
        changedProduct.setIsArchived(product.get().getIsArchived());
        try{
            return ResponseEntity.ok().body(new ProductResponse(
                    this.productsRepository.save(changedProduct)
            ));
        }
        catch (TransientObjectException e){
            return ResponseEntity.badRequest().body(new ErrorResponse("Błędni autorzy lub typy"));
        }
    }


    @PreAuthorize("hasAuthority('products:write')")
    @DeleteMapping(value = "deleteProduct/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        BookProduct removingProduct = this.productsRepository.findById(id).orElse(null);
        if(removingProduct==null) return ResponseEntity.ok().build();
        productsRepository.deleteById(id);
//        this.productsImagesManager.deleteProductImages(id);
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
        productsImagesManager.saveAndScale(new DatabaseImage(new ImageIdentifier(productId, null),
                fileType, file.getBytes()));
        return ResponseEntity.ok().build();
    }


    @PreAuthorize("hasAuthority('products:write')")
    @DeleteMapping("deleteProductImage/{id}")
    public ResponseEntity<?> deleteProductImage(@PathVariable("id") Long productId) {
        this.productsImagesManager.deleteProductImages(productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("downloadProductOriginalImage/{id}")
    public ResponseEntity<?> downloadOriginalProductImage(@PathVariable("id") Long productId) {
        return this.downloadProductImage(this.productsImagesManager.getOriginalImage(productId));
    }

    @GetMapping("downloadProductSmallImage/{id}")
    public ResponseEntity<?> downloadSmallProductImage(@PathVariable("id") Long productId) {
        return this.downloadProductImage(this.productsImagesManager.getSmallImage(productId));
    }

    @GetMapping("downloadProductIcon/{id}")
    public ResponseEntity<?> downloadProductIcon(@PathVariable("id") Long productId){
        return this.downloadProductImage(this.productsImagesManager.getIcon(productId));
    }

    private ResponseEntity<?> downloadProductImage(Optional<DatabaseImage> image) {
        if (image.isEmpty())
            return ResponseEntity.badRequest().body(new ErrorResponse("Ten produkt nie ma obrazu"));
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(image.get().getMediaType()))
                .body(image.get().getImage());
    }

    private BookProduct newProduct(ProductRequest request) {
        return BookProduct.builder()
                .name(request.getName())
                .price(Math.floor(request.getPrice() * 100) / 100)
                .description(request.getDescription())
                .authors(request.getAuthors().stream().map(id->Author.builder().id(id).build()).collect(Collectors.toSet()))
                .types(request.getTypes().stream().map(id->CommonType.builder().id(id).build()).collect(Collectors.toSet()))
                .inStock(request.getInStock()).build();
    }

    private BookProduct fromRequest(Long id, ProductRequest request) {
        BookProduct bp = this.newProduct(request);
        bp.setId(id);
        return bp;
    }
}
