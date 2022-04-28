package me.practice.shop.shop.controllers.products.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @PositiveOrZero
    @NotNull
    private Double price;

    @NotNull
    private Set<String> types;

    @NotNull
    private Set<String> authorsNames;

    @PositiveOrZero
    private Integer inStock;

}
