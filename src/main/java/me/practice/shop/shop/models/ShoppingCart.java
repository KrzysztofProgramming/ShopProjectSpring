package me.practice.shop.shop.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;

@Document("shopping_carts")
@Data
@AllArgsConstructor
public class ShoppingCart {
    @Id
    private String ownerUsername;

    private Map<String, Integer> items;
    private Date expireDate;
}
