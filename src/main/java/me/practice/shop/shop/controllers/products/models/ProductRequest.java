package me.practice.shop.shop.controllers.products.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;
import java.util.stream.Collectors;

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

    private void setTypes(Collection<String> types){
        this.types = types.stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    @NotNull
    private Collection<String> authorsNames;

    @PositiveOrZero
    private Integer inStock;

}
