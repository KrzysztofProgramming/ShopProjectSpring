package me.practice.shop.shop.database.users;

import me.practice.shop.shop.controllers.users.models.GetUsersParams;
import me.practice.shop.shop.models.ShopUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class UsersSearcherImpl implements UsersSearcher {
    @Override
    public Page<ShopUser> findByGetParams(GetUsersParams params, Pageable pageable) {
        return null; //todo
    }
}
