package me.practice.shop.shop.controllers.authors.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class AuthorRequest {
    @NotEmpty
    private String name;
    @NotNull
    private String description;
}
