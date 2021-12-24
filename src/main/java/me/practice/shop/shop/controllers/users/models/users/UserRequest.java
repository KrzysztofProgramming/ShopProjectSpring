package me.practice.shop.shop.controllers.users.models.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.practice.shop.shop.validators.password.Password;
import me.practice.shop.shop.validators.username.Username;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

    @Username
    private String username;
    @Email
    private String email;
    @Password
    private String password;
    @NotNull
    private Collection<String> roles;
}
