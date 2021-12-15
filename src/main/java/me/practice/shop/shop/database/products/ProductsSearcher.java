package me.practice.shop.shop.database.products;

import me.practice.shop.shop.controllers.products.models.GetProductsParams;
import me.practice.shop.shop.models.BookProduct;
import org.springframework.data.domain.Page;

public interface ProductsSearcher {
    Page<BookProduct> findByParams(GetProductsParams params);
}
