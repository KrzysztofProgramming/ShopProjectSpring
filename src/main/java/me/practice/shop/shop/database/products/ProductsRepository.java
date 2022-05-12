package me.practice.shop.shop.database.products;

import me.practice.shop.shop.models.BookProduct;
import me.practice.shop.shop.models.SimpleAuthor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProductsRepository extends JpaRepository<BookProduct, Long> {
    Optional<BookProduct> findByName(String name);

    @Query(value = "SELECT b FROM #{#entityName} b LEFT JOIN b.authors a WHERE a.id = ?1")
    Collection<BookProduct> getByAuthorId(Long authorId);

    @Query(value = "SELECT b FROM #{#entityName} b LEFT JOIN b.types t WHERE t.name IN ?1")
    Collection<BookProduct> getByTypes(Collection<String> typesNames);

    @Query(value = "SELECT b FROM #{#entityName} b LEFT JOIN b.authors a WHERE a.id IN ?1")
    Collection<BookProduct> getByAuthorsIds(Collection<Long> authorsIds);

    @Query(value = "SELECT COUNT(t) FROM #{#entityName} b JOIN b.types t GROUP BY t.id HAVING t.id = ?1")
    long countByType(Long typeId);

    @Query(value = "SELECT COUNT(a) FROM #{#entityName} b JOIN b.authors a GROUP BY a.id HAVING a.id = ?1")
    long countByAuthor(Long authorId);

    @Query(value = "SELECT NEW me.practice.shop.shop.models.SimpleAuthor(a.id, a.name) " +
            "FROM #{#entityName} b LEFT JOIN b.authors a WHERE b.id = ?1" )
    Set<SimpleAuthor> getBookSimpleAuthors(Long id);

    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM order_products_ids c WHERE c.product_id = ?1")
    long getProductUsageInOrders(long id);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE #{#entityName} b SET b.isArchived = ?2 WHERE b.id = ?1")
    int archiveProduct(long id, boolean value);

    @Query("SELECT b FROM #{#entityName} b WHERE b.id IN ?1")
    List<BookProduct> findAllByIds(Collection<Long> ids, Sort sort);

    @Query("SELECT b FROM #{#entityName} b WHERE b.id IN ?1 AND b.isArchived = ?2")
    List<BookProduct> getByIdsAndArchive(Collection<Long> ids, Boolean archiveStatus);




}
