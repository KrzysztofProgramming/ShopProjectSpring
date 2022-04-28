package me.practice.shop.shop.controllers.products.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import me.practice.shop.shop.models.CommonType;

@Data
@AllArgsConstructor
public class TypeDetailsResponse {
    private Long id;
    private String name;
    private Long productsCount;

    public TypeDetailsResponse(CommonType type, Long productsCount){
        this(type.getId(), type.getName(), productsCount);
    }
}
