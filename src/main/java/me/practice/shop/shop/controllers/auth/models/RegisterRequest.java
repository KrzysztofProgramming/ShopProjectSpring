package me.practice.shop.shop.controllers.auth.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.practice.shop.shop.validators.username.Username;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @Email(message = "not a valid email format")
    private String email;

    @Username
    @Size(min = 4, max = 20, message = "nick must be between 4 and 20 characters length")
    private String username;

    @NotBlank(message = "password cannot be blank")
    @Size(min = 4, max = 40, message = "password must be between 4 and 40 characters length")
    private String password;
}
