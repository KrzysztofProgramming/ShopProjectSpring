package me.practice.shop.shop.controllers.products.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.practice.shop.shop.models.CommonType;

@Data
@AllArgsConstructor
public class TypeResponse {
    private Long id;
    private String name;

    public TypeResponse(CommonType type){
        this(type.getId(), type.getName());
    }
}
