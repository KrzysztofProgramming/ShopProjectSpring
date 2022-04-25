package me.practice.shop.shop.database.authors;

import me.practice.shop.shop.controllers.authors.models.SimpleAuthor;
import me.practice.shop.shop.models.Author;
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

    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM books_authors ba WHERE ba.fk_author = ?1")
    int countAuthorBooks(Long authorId);

    @Query(value = "SELECT NEW me.practice.shop.shop.controllers.authors.models.SimpleAuthor(a.id, a.name) " +
            "FROM #{#entityName} a")
    List<SimpleAuthor> getSimpleAuthorsList();
}
