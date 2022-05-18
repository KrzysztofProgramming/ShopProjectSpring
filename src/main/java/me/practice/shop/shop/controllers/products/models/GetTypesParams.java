package me.practice.shop.shop.controllers.products.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.practice.shop.shop.models.PageableParams;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetTypesParams extends PageableParams {
    private String searchPhrase = "";
    @PositiveOrZero
    private Integer maxBooks = null;
    @PositiveOrZero
    private Integer minBooks = null;
    @NotNull
    private String sort = "";
}
