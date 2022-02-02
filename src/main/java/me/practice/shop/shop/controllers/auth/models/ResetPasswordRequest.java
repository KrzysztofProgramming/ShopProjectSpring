package me.practice.shop.shop.controllers.auth.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {
    @Email
    private String email;
}
