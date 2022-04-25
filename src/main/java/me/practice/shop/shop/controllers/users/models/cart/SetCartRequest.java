package me.practice.shop.shop.controllers.users.models.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Map;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class SetCartRequest {
    @NotNull
    private Map<Long, Integer> products;
}
