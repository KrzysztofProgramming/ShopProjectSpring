package me.practice.shop.shop.controllers.users.models.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.practice.shop.shop.validators.products.ProductsCountsMap;

import javax.validation.constraints.NotNull;
import java.util.Map;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class SetCartRequest {

    @ProductsCountsMap()
    @NotNull
    private Map<Long, Integer> products;
}
