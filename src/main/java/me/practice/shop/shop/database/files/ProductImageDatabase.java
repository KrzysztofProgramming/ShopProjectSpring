package me.practice.shop.shop.database.files;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImageDatabase extends MongoRepository<ProductImage, String> {

}
