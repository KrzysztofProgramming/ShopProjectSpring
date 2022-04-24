package me.practice.shop.shop.database.products;

import me.practice.shop.shop.models.BookProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface ProductsRepository extends JpaRepository<BookProduct, Long> {
    Optional<BookProduct> findByName(String name);

    @Query(value = "SELECT b FROM #{#entityName} b LEFT JOIN b.authors a WHERE a.id = ?1")
    Collection<BookProduct> getByAuthorId(Long authorId);

    @Query(value = "SELECT b FROM #{#entityName} b LEFT JOIN b.types t WHERE t.name IN ?1")
    Collection<BookProduct> getByTypes(Collection<String> typesNames);

    @Query(value = "SELECT b FROM #{#entityName} b LEFT JOIN b.authors a WHERE a.id IN ?1")
    Collection<BookProduct> getByAuthorsIds(Collection<Long> authorsIds);

    @Query(value = "SELECT COUNT(b) FROM #{#entityName} b LEFT JOIN b.types t WHERE t.name = ?1")
    long countByType(String type);
}
