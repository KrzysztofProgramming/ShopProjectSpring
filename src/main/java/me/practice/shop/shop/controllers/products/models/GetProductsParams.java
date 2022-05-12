package me.practice.shop.shop.controllers.products.models;

import lombok.*;
import me.practice.shop.shop.models.PageableParams;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetProductsParams extends PageableParams {

    public static final int ALL_PRODUCTS = -1;
    public static final int ARCHIVED_PRODUCTS = 1;
    public static final int AVAILABLE_PRODUCTS = 0;

    private String searchPhrase = null;

    @PositiveOrZero
    private Double minPrice = null;

    @PositiveOrZero
    private Double maxPrice = null;

    @NotNull
    private List<Long> types = new ArrayList<>();

    private String sort = null;

    @NotNull
    private List<Long> authors = new ArrayList<>();

    @PositiveOrZero
    private Integer minInStock = null;
    @PositiveOrZero
    private Integer maxInStock = null;

    private Integer isArchived = AVAILABLE_PRODUCTS;
}
