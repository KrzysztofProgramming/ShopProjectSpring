package me.practice.shop.shop.controllers.products.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.practice.shop.shop.models.BookProduct;

import java.util.Collection;

@Data
@AllArgsConstructor
public class GetProductsResponse {
    private int pageNumber;
    private int totalPages;
    private long totalElements;
    private Collection<BookProduct> products;
}
