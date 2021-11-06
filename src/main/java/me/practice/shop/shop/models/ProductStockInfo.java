package me.practice.shop.shop.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("products_stocks")
@AllArgsConstructor
@Data
public class ProductStockInfo {
    @Id
    private String productId;
    private Integer amount;
}
