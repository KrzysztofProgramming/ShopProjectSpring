package me.practice.shop.shop.controllers.authors.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.practice.shop.shop.models.Author;

@AllArgsConstructor
@Data
public class AuthorResponse {
    private Long id;
    private String name;
    private String description;
    private Integer writtenBooks;

    public AuthorResponse(Author author){
        this(author.getId(), author.getName(), author.getDescription(), author.getWrittenBooks());
    }
}
