package me.practice.shop.shop.controllers.auth.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.practice.shop.shop.controllers.perms.models.RoleResponse;

import java.util.Collection;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String jwtToken;
    private String refreshToken;
    private Collection<RoleResponse> roles;
}
