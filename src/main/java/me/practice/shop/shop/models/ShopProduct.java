package me.practice.shop.shop.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;


@Document(value = "products")
@Data
@AllArgsConstructor
public class ShopProduct {

    @Id
    private String id;

    @TextIndexed(weight = 3.f)
    private String name;

    @Indexed
    private Double price;

    @TextIndexed
    private String description;

    @Indexed
    @TextIndexed(weight = 2.f)
    private Collection<String> types;

    private Integer inStore;
}
