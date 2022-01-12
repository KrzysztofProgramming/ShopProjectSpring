package me.practice.shop.shop.database.products;

import me.practice.shop.shop.models.CommonType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommonTypesRepository extends MongoRepository<CommonType, String> {

    @Query("{name: {$in: ?0}}")
    Iterable<CommonType> getAllByNames(Iterable<String> names);
}
