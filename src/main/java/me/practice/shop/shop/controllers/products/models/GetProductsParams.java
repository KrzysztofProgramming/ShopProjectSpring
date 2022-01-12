package me.practice.shop.shop.controllers.products.models;

import lombok.*;
import me.practice.shop.shop.models.PageableParams;

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

//    @PositiveOrZero
    private Double minPrice = -1.d;

//    @PositiveOrZero
    private Double maxPrice = -1.d;

    private List<String> types = new ArrayList<>();

    public void setTypes(List<String> types){
        this.types = types.stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    private String sort = "";

    private List<String> authorsNames = new ArrayList<>();

    private Integer minInStock = -1;
    private Integer maxInStock = -1;
}
