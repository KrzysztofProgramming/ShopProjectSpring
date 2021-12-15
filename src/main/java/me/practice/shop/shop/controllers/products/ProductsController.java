package me.practice.shop.shop.controllers.products;

import me.practice.shop.shop.controllers.products.models.GetProductsParams;
import me.practice.shop.shop.controllers.products.models.GetProductsResponse;
import me.practice.shop.shop.controllers.products.models.ProductRequest;
import me.practice.shop.shop.database.authors.Author;
import me.practice.shop.shop.database.files.DatabaseImage;
import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.models.BookProduct;
import me.practice.shop.shop.models.ErrorResponse;
import me.practice.shop.shop.utils.MediaTypeUtils;
import org.bson.types.Binary;
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    @GetMapping(value = "getAll")
    public ResponseEntity<?> getProducts(@Valid GetProductsParams params) {
        Page<BookProduct> productPage = this.productsRepository.findByParams(params);
        return ResponseEntity.ok(new GetProductsResponse(productPage.getNumber() + 1, productPage.getTotalPages(),
                productPage.getTotalElements(), productPage.toList()));
    }

    @GetMapping(value = "byId/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {
        Optional<BookProduct> product = productsRepository.findById(id);
        return product.isPresent() ? ResponseEntity.ok(product)
                : ResponseEntity.badRequest().body(productNotExistsInfo);
    }

    @GetMapping(value = "byIds")
    public ResponseEntity<?> getProductsByIds(@RequestParam @Valid @NotEmpty List<String> ids){
        Iterable<BookProduct> products = productsRepository.findAllById(ids);
        return ResponseEntity.ok(StreamSupport.stream(products.spliterator(), false).collect(Collectors.toList()));
    }


    @PreAuthorize("hasAuthority('PRODUCTS_MODIFY')")
    @PostMapping(value = "addNewProduct")
    public ResponseEntity<?> addProduct(@Valid @RequestBody ProductRequest request) {
        Optional<Collection<Author>> authors = this.authorsManager.validateAndSaveAuthors(request.getAuthors());
        if(authors.isEmpty()) return ResponseEntity.badRequest().body(new ErrorResponse("Zły autor/autorzy"));
        return ResponseEntity.ok().body(productsRepository.insert(this.newProduct(request, authors.get())));
    }

    @PreAuthorize("hasAuthority('PRODUCTS_MODIFY')")
    @PutMapping(value = "updateProduct/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable String id, @Valid @RequestBody ProductRequest request) {
        Optional<Collection<Author>> authors = this.authorsManager.validateAndSaveAuthors(request.getAuthors());
        if(authors.isEmpty()) return ResponseEntity.badRequest().body(new ErrorResponse("Zły autor/autorzy"));

        Optional<BookProduct> product =  productsRepository.findById(id)
                    .map(shopProduct ->
                            productsRepository.save(this.fromRequest(shopProduct.getId(), request, authors.get())));
        if(product.isEmpty())
            return ResponseEntity.badRequest().body(productNotExistsInfo);

         return ResponseEntity.ok(product.get());
    }

    @PreAuthorize("hasAuthority('PRODUCTS_MODIFY')")
    @DeleteMapping(value = "deleteAuthor/{id}")
    public ResponseEntity<?> deleteAuthor(@PathVariable String id){
        return this.authorsManager.deleteAuthor(id) ? ResponseEntity.ok().build(): ResponseEntity.badRequest().body(
                new ErrorResponse("Niektóre produkty korzystają z tego autora, zmień to przed usunięciem go"));
    }

    @PreAuthorize("hasAuthority('PRODUCTS_MODIFY')")
    @DeleteMapping(value = "deleteProduct/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable String id){
        productsRepository.deleteById(id);
        this.productsImagesRepository.deleteProductImages(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('PRODUCTS_MODIFY')")
    @PutMapping("uploadProductImage/{id}")
    public ResponseEntity<?> uploadProductImage(@PathVariable("id") String productId,
                                                @RequestParam("file") MultipartFile file) throws IOException {
        if(!productsRepository.existsById(productId)){
            return ResponseEntity.badRequest().body(this.productNotExistsInfo);
        }
        String fileType = MediaTypeUtils.detectFileType(file.getBytes());
        if(!MediaTypeUtils.isImageTypeOK(fileType)){
            return ResponseEntity.badRequest().body(new ErrorResponse("Zły typ pliku"));
        }
        productsImagesRepository.saveAndScale(new DatabaseImage(productId, fileType, new Binary(file.getBytes())));
        return ResponseEntity.ok().build();
    }



    @PreAuthorize("hasAuthority('PRODUCTS_MODIFY')")
    @DeleteMapping("deleteProductImage/{id}")
    public ResponseEntity<?> deleteProductImage(@PathVariable("id") String productId){
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

    private ResponseEntity<?> downloadProductImage(Optional<DatabaseImage> image){
        if(image.isEmpty())
            return ResponseEntity.badRequest().body(new ErrorResponse("Ten produkt nie ma obrazu"));
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(image.get().getMediaType()))
                .body(image.get().getImage().getData());
    }

    private BookProduct newProduct(ProductRequest request, Collection<Author> authors){
        return fromRequest(UUID.randomUUID().toString(), request, authors);
    }

    private BookProduct fromRequest(String id, ProductRequest request, Collection<Author> authors) {
        return new BookProduct(id, request.getName(),Math.floor(request.getPrice() * 100) / 100,
                request.getDescription(),
                authors,
                request.getTypes().stream().map(String::toUpperCase).collect(Collectors.toList()), request.getInStock());
    }
}
