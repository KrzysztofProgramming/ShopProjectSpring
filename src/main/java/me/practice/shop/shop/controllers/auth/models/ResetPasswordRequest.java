package me.practice.shop.shop.controllers.auth.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.practice.shop.shop.validators.password.Password;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {
    @NotEmpty
    private String token;

    @Password
    private String newPassword;
}
