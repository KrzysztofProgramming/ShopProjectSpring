package me.practice.shop.shop.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.practice.shop.shop.database.authors.Author;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;


@Document("books_database")
@Data
@AllArgsConstructor
public class BookProduct {

    @Id
    private String id;

    @TextIndexed(weight = 3.f)
    private String name;

    @Indexed
    private Double price;

    @TextIndexed
    private String description;

    @DBRef(lazy = true)
    private Collection<Author> authors;

    @Indexed
    @TextIndexed(weight = 2.f)
    private Collection<String> types;

    @Indexed
    private Integer inStock;
}
