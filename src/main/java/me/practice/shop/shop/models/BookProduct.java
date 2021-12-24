package me.practice.shop.shop.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TextScore;

import java.util.Collection;


@Document(value = "books_database")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data()
public class BookProduct {

    @Id
    @EqualsAndHashCode.Include
    private String id;

    @TextIndexed(weight = 5.f)
    private String name;

    @Indexed
    private Double price;

    @TextIndexed
    private String description;

    @DBRef(lazy = true)
    private Collection<Author> authors;

    @TextIndexed(weight = 2.f)
    private Collection<String> authorsNames;

    @Indexed
    @TextIndexed(weight = 3.f)
    private Collection<String> types;

    @Indexed
    private Integer inStock;

    @TextScore
    private float textScore;

    public BookProduct(String id, String name, Double price, String description, Collection<Author> authors,
                       Collection<String> authorsNames, Collection<String> types, Integer inStock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.authors = authors;
        this.authorsNames = authorsNames;
        this.types = types;
        this.inStock = inStock;
    }
}
