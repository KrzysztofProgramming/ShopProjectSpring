package me.practice.shop.shop.database.products.types;

import me.practice.shop.shop.models.CommonType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommonTypesRepository extends MongoRepository<CommonType, String>, TypesSearcher {

    @Query("{name: {$in: ?0}}")
    Iterable<CommonType> getAllByNames(Iterable<String> names);

    @Query("{name: ?0}")
    Optional<CommonType> findByName(String name);

    @Query( value = "{name: ?0}", delete = true)
    void deleteByName(String name);
}

