package me.practice.shop.shop.database.products;

import me.practice.shop.shop.controllers.products.models.GetProductsParams;
import me.practice.shop.shop.models.BookProduct;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.Map;

public interface ProductsSearcher {
    Page<BookProduct> findByParams(GetProductsParams params);
    boolean allExistByIds(Collection<String> ids);
//    BulkWriteResult decreaseProductsCounts(Map<String, Integer> productsCounts);
    boolean allProductsAvailable(Map<String, Integer> products);
}
