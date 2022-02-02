package me.practice.shop.shop.controllers.users.models.cart;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Data
public class CartWithDetailsResponse {
    private String ownerUsername;
    private Date expireDate;
    private List<CartDetail> items;
}
