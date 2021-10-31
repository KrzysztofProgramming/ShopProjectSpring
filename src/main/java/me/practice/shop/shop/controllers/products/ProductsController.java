package me.practice.shop.shop.controllers.products;

import me.practice.shop.shop.controllers.products.models.GetProductsParams;
import me.practice.shop.shop.controllers.products.models.GetProductsResponse;
import me.practice.shop.shop.controllers.products.models.ProductRequest;
import me.practice.shop.shop.database.files.DatabaseImage;
import me.practice.shop.shop.database.products.ProductsDatabase;
import me.practice.shop.shop.models.ErrorResponse;
import me.practice.shop.shop.models.ShopProduct;
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
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RequestMapping(value = "api/products/")
public class ProductsController {

    private final ErrorResponse productNotExistsInfo = new ErrorResponse(
            "Taki produkt nie istnieje lub już został usunięty");

    @Autowired
    private ProductsDatabase productsDatabase;

    @Autowired
    private ProductsImagesRepository productsImagesRepository;


    @GetMapping(value = "getAll")
    public ResponseEntity<?> getProducts(@Valid GetProductsParams params) {

        Page<ShopProduct> productPage = this.productsDatabase.findByParams(params);
        return ResponseEntity.ok(new GetProductsResponse(productPage.getNumber() + 1, productPage.getTotalPages(),
                productPage.getTotalElements(), productPage.toList()));
    }

    @GetMapping(value = "byId/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {
        Optional<ShopProduct> product = productsDatabase.findById(id);
        return product.isPresent() ? ResponseEntity.ok(product)
                : ResponseEntity.badRequest().body(productNotExistsInfo);
    }

    @PreAuthorize("hasAuthority('PRODUCTS_MODIFY')")
    @PostMapping(value = "addNewProduct")
    public ResponseEntity<?> addProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok().body(productsDatabase.insert(this.newProduct(request)));
    }
//
//    @PreAuthorize("hasAuthority('PRODUCTS_MODIFY')")
//    @PostMapping(value = "test")
//    public ResponseEntity<?> testApi(@RequestParam("file") MultipartFile file) {
//        return ResponseEntity.ok().build();
//    }

    @PreAuthorize("hasAuthority('PRODUCTS_MODIFY')")
    @PutMapping(value = "updateProduct/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable String id, @Valid @RequestBody ProductRequest request) {
          Optional<ShopProduct> product =  productsDatabase.findById(id)
                    .map(shopProduct -> {
                        shopProduct.setName(request.getName());
                        shopProduct.setPrice(request.getPrice());
                        shopProduct.setDescription(request.getDescription());
                        return productsDatabase.save(shopProduct);
            });
          if(product.isEmpty())
              return ResponseEntity.badRequest().body(productNotExistsInfo);

          return ResponseEntity.ok(product.get());
    }

    @PreAuthorize("hasAuthority('PRODUCTS_MODIFY')")
    @DeleteMapping(value = "deleteProduct/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable String id){
        productsDatabase.deleteById(id);
        this.productsImagesRepository.deleteProductImages(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('PRODUCTS_MODIFY')")
    @PutMapping("uploadProductImage/{id}")
    public ResponseEntity<?> uploadProductImage(@PathVariable("id") String productId,
                                                @RequestParam("file") MultipartFile file) throws IOException {
        if(!productsDatabase.existsById(productId)){
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

    private ShopProduct newProduct(ProductRequest request){
        return fromRequest(UUID.randomUUID().toString(), request);
    }

    private ShopProduct fromRequest(String id, ProductRequest request) {
        return new ShopProduct(id, request.getName(),Math.floor(request.getPrice() * 100) / 100, request.getDescription(),
                request.getTypes().stream().map(String::toUpperCase).collect(Collectors.toList()), request.getInStore());
    }

}
