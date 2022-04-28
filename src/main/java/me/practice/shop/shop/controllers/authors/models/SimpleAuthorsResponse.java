package me.practice.shop.shop.controllers.authors.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.practice.shop.shop.models.SimpleAuthor;

import java.util.List;

@Data
@AllArgsConstructor
public class SimpleAuthorsResponse {
    private List<SimpleAuthor> simpleAuthors;
}
