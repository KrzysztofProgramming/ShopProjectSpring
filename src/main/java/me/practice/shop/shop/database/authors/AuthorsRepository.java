package me.practice.shop.shop.database.authors;

import me.practice.shop.shop.controllers.authors.models.AuthorResponse;
import me.practice.shop.shop.models.Author;
import me.practice.shop.shop.models.SimpleAuthor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AuthorsRepository extends JpaRepository<Author, Long> {


    @Query(value = "SELECT a FROM #{#entityName} a WHERE a.name = ?1")
    Optional<Author> findByName(String name);

    @Query(value = "SELECT a FROM #{#entityName} a WHERE a.name IN ?1")
    Set<Author> findAllByNames(Collection<String> names);

    @Query(value = "SELECT DISTINCT a.name FROM #{#entityName} a")
    Collection<String> getAuthorsNames();

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE #{#entityName} a SET a.name = ?2, a.description = ?3 WHERE a.id = ?1")
    int updateNameAndDescription(Long id, String name, String description);

    @Query(value = "SELECT COUNT(a) FROM BookProduct b JOIN b.authors a GROUP BY a.id HAVING a.id = ?1")
    long countAuthorBooks(Long authorId);

    @Query(value = "SELECT NEW me.practice.shop.shop.models.SimpleAuthor(a.id, a.name) " +
            "FROM #{#entityName} a")
    List<SimpleAuthor> getSimpleAuthorsList();

    @Query(value = "SELECT NEW me.practice.shop.shop.controllers.authors.models.AuthorResponse(" +
            "a.id, a.name, a.description, COUNT(a) as b_count) " +
            "FROM #{#entityName} a JOIN a.books b GROUP BY a.id")
    Page<AuthorResponse> getAuthorResponses(Pageable pageable);

    @Query(value = "SELECT NEW me.practice.shop.shop.controllers.authors.models.AuthorResponse(" +
            "a.id, a.name, a.description, COUNT(a)) " +
            "FROM #{#entityName} a JOIN a.books b GROUP BY a.id HAVING a.id = ?1")
    Optional<AuthorResponse> getAuthorResponseById(Long id);

    @Query(value = "SELECT NEW me.practice.shop.shop.controllers.authors.models.AuthorResponse(" +
            "a.id, a.name, a.description, COUNT(b)) " +
            "FROM #{#entityName} a LEFT JOIN a.books b GROUP BY a.id HAVING" +
            " (:maxBooks IS NULL OR COUNT(b) <= :maxBooks)" +
            " AND (:minBooks IS NULL OR COUNT(b) >= :minBooks)" +
            " AND (:search IS NULL OR a.name LIKE :search)")
    Page<AuthorResponse> findByParams(@Param("maxBooks") Integer maxBooks,
                                      @Param("minBooks") Integer minBooks,
                                      @Param("search") String searchPhrase,
                                      Pageable pageable);

}
