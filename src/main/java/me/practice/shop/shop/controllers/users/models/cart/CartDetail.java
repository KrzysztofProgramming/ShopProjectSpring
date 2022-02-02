package me.practice.shop.shop.controllers.users.models.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.practice.shop.shop.controllers.products.models.ProductResponse;

@Data
@AllArgsConstructor
public class CartDetail {
    private int amount;
    private ProductResponse product;
}
