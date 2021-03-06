package me.practice.shop.shop.controllers.products.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.models.BookProduct;
import me.practice.shop.shop.models.SimpleAuthor;

import java.util.Collection;
import java.util.stream.Collectors;

@AllArgsConstructor
@Data
public class ProductResponse {
    private Long id;
    private String name;
    private Double price;
    private String description;
    private Collection<SimpleAuthor> authors;
    private Collection<TypeResponse> types;
    private Integer inStock;
    private Boolean isArchived;
    private Boolean isDeletable;

    public ProductResponse(BookProduct product){
        this(product.getId(),product.getName(), product.getPrice(), product.getDescription(),
                product.getAuthors().stream().map(SimpleAuthor::new).collect(Collectors.toSet()),
                product.getTypes().stream().map(TypeResponse::new).collect(Collectors.toSet()),
                product.getInStock(), product.getIsArchived(), null);
    }

    public ProductResponse(BookProduct product, ProductsRepository repository){
        this(product);
        this.isDeletable = repository.getProductUsageInOrders(product.getId()) <= 0;
    }
}
