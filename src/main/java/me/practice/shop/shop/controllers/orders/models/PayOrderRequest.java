package me.practice.shop.shop.controllers.orders.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayOrderRequest {
    @NotEmpty
    private String id;
}
