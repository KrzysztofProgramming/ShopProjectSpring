package me.practice.shop.shop.controllers.auth.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetTokenValidationRequest {
    @NotEmpty
    private String token;
}
