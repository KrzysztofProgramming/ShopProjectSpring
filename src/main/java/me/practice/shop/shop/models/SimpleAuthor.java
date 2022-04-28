package me.practice.shop.shop.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SimpleAuthor {
    private Long id;
    private String name;

    public SimpleAuthor(Author author){
        this.id = author.getId();
        this.name = author.getName();
    }
}
