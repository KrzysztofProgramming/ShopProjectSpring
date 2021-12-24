package me.practice.shop.shop.controllers.products.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.practice.shop.shop.controllers.authors.models.AuthorResponse;
import me.practice.shop.shop.models.BookProduct;

import java.util.Collection;
import java.util.stream.Collectors;

@AllArgsConstructor
@Data
public class ProductResponse {
    private String id;
    private String name;
    private Double price;
    private String description;
    private Collection<AuthorResponse> authors;
    private Integer inStock;

    public ProductResponse(BookProduct product){
        this(product.getId(),product.getName(), product.getPrice(), product.getDescription(),
                product.getAuthors().stream().map(AuthorResponse::new).collect(Collectors.toList()),
                product.getInStock());
    }
}
