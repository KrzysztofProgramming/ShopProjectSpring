package me.practice.shop.shop.controllers.users.models.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartProductRequest {
    @PositiveOrZero
    @NotNull
    private Long productId;
    @Positive
    @NotNull
    private Integer amount;
}
