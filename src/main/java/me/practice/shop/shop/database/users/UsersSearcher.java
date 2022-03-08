package me.practice.shop.shop.database.users;

import com.mongodb.client.result.UpdateResult;
import me.practice.shop.shop.controllers.users.models.users.GetUsersParams;
import me.practice.shop.shop.models.ShopUser;
import me.practice.shop.shop.models.UserInfo;
import org.springframework.data.domain.Page;

public interface UsersSearcher {

    Page<ShopUser> findByGetParams(GetUsersParams params);
    UpdateResult saveUserInfo(String id, UserInfo info);
}
