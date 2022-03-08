package me.practice.shop.shop.controllers.users.models.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.practice.shop.shop.models.UserInfo;

@Data
@AllArgsConstructor
public class ProfileResponse {
    private String username;
    private String email;
    private UserInfo info;
}
