package me.practice.shop.shop.controllers.users.models.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartProductRequest {
    @NotBlank
    private String productId;
    @Positive
    private Integer amount;
}
