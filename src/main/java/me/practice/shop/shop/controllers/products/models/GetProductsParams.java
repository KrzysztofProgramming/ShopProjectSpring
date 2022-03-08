package me.practice.shop.shop.controllers.products.models;

import lombok.*;
import me.practice.shop.shop.models.CommonType;
import me.practice.shop.shop.models.PageableParams;

import javax.validation.constraints.PositiveOrZero;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private List<String> types = new ArrayList<>();

    public void setTypes(List<String> types){
        this.types = types.stream().map(CommonType::toTypeName).collect(Collectors.toList());
    }

    private String sort = "";

    private List<String> authorsNames = new ArrayList<>();

    @PositiveOrZero
    private Integer minInStock = null;
    @PositiveOrZero
    private Integer maxInStock = null;
}
