package me.practice.shop.shop.controllers.products.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.practice.shop.shop.models.PageableParams;

import javax.validation.constraints.PositiveOrZero;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class GetTypesParams extends PageableParams {
    private String searchPhrase = "";
    @PositiveOrZero
    private Integer minProducts = null;
    @PositiveOrZero
    private Integer maxProducts = null;
}
