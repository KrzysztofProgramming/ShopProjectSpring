package me.practice.shop.shop.controllers.orders.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.practice.shop.shop.models.UserInfo;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewOrderRequest {
    @NotNull
    @Valid
    private UserInfo info;
    @NotEmpty
    private Map<String, Integer> products;
    @Email
    @NotNull
    private String email;
}
