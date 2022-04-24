package me.practice.shop.shop.database.authors;

import me.practice.shop.shop.models.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorsRepository extends JpaRepository<Author, Long> {


    @Query(value = "SELECT a FROM #{#entityName} a WHERE a.name = ?1")
    Optional<Author> findByName(String name);

    @Query(value = "SELECT a WHERE #{#entityName} a WHERE a.name in ?1")
    Iterable<Author> findAllByNames(Iterable<String> names);

}
