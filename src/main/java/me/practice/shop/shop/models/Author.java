package me.practice.shop.shop.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TextScore;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document(Author.COLLECTION_NAME)
public class Author {
    public final static String COLLECTION_NAME = "authors_list";

    @Id
    @EqualsAndHashCode.Include
    private String id;

    @TextIndexed
    @Indexed(unique = true)
    private String name;

    private String description;

    private int writtenBooks;

    @TextScore
    private float textScore;


    public Author(String id, String name, String description, int writtenBooks) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.writtenBooks = writtenBooks;
    }
}
