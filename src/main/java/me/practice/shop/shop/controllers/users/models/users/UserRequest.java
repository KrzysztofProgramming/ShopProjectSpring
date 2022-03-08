package me.practice.shop.shop.controllers.users.models.users;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.practice.shop.shop.models.UserInfo;
import me.practice.shop.shop.validators.password.Password;
import me.practice.shop.shop.validators.username.Username;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.stream.Collectors;

@Data
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
    @Valid
    private UserInfo info;

    public void setRoles(Collection<String> roles) {
        this.roles = roles.stream().map(String::toLowerCase).collect(Collectors.toList());
    }
}
