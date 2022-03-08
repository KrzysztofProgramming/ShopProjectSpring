package me.practice.shop.shop.database.orders;

import me.practice.shop.shop.controllers.users.models.profile.GetOrdersParams;
import me.practice.shop.shop.models.ShopOrder;
import org.springframework.data.domain.Page;

public interface OrdersSearcher {
    Page<ShopOrder> getByParams(GetOrdersParams params, String ownerUsername);
}
