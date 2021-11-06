package me.practice.shop.shop.database.users;

import me.practice.shop.shop.controllers.users.models.users.GetUsersParams;
import me.practice.shop.shop.models.ShopUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsersSearcher {

    Page<ShopUser> findByGetParams(GetUsersParams params, Pageable pageable);
}
