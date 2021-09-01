package me.practice.shop.shop.controllers.users.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.practice.shop.shop.validators.username.Username;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

    @Username
    private String username;
    @Email
    private String email;
    @NotEmpty
    private String password;
    private Collection<String> authorities;
    private Collection<String> roles;
}
