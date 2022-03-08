package me.practice.shop.shop.controllers.users.models.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserResponse {
    private String username;
    private String email;
    private long authorities;
    private Collection<String> roles;
}
