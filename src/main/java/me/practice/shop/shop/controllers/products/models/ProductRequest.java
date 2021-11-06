package me.practice.shop.shop.controllers.products.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @PositiveOrZero
    private Double price;

    @NotNull
    private Collection<String> types;

    @PositiveOrZero
    private Integer inStock;

}
