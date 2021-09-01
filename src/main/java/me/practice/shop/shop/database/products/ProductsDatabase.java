package me.practice.shop.shop.database.products;

import me.practice.shop.shop.models.ShopProduct;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductsDatabase extends MongoRepository<ShopProduct, String>, ProductsSearcher {
    Optional<ShopProduct> findByName(String name);
}
