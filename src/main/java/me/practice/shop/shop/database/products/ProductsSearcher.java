package me.practice.shop.shop.database.products;

import me.practice.shop.shop.controllers.products.models.GetProductsParams;
import me.practice.shop.shop.models.ShopProduct;
import org.springframework.data.domain.Page;

public interface ProductsSearcher {
    Page<ShopProduct> findByParams(GetProductsParams params);
}
