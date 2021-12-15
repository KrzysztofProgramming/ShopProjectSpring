package me.practice.shop.shop.database.products;

import me.practice.shop.shop.models.BookProduct;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductsRepository extends MongoRepository<BookProduct, String>, ProductsSearcher {
    Optional<BookProduct> findByName(String name);

    @Query("{authors: {$elemMatch: {$id: ?0}}}")
    Iterable<BookProduct> getByAuthorId(String authorId);
}
