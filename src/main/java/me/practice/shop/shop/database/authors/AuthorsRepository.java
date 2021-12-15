package me.practice.shop.shop.database.authors;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorsRepository extends MongoRepository<Author, String> {
    Optional<Author> findByName();

    @Query("{name: {$in: ?0}}")
    Iterable<Author> findAllByNames(Iterable<String> names);

}
