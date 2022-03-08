package me.practice.shop.shop.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderProductDetail {
    private BookProduct product;
    private int amount;
}
