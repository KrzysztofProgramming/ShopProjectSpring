package me.practice.shop.shop.controllers.products.models;

import lombok.*;
import me.practice.shop.shop.models.PageableParams;

import javax.validation.constraints.PositiveOrZero;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetProductsParams extends PageableParams {

    private String searchPhrase = "";

    @PositiveOrZero
    private Double minPrice = null;

    @PositiveOrZero
    private Double maxPrice = null;

    private List<Long> types = new ArrayList<>();

    private String sort = "";

    private List<Long> authorsNames = new ArrayList<>();

    @PositiveOrZero
    private Integer minInStock = null;
    @PositiveOrZero
    private Integer maxInStock = null;
}
