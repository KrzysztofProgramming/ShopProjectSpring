package me.practice.shop.shop.database.products;

import me.practice.shop.shop.models.CommonType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommonTypesRepository extends MongoRepository<CommonType, String> {
}
