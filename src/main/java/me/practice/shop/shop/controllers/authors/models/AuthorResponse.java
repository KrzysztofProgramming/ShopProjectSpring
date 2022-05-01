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
    private Long writtenBooks;

    public AuthorResponse(Author author, Long writtenBooks){
        this(author.getId(), author.getName(), author.getDescription(), writtenBooks);
    }
}
