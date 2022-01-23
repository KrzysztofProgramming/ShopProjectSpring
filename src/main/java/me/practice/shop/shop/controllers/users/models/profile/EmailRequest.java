package me.practice.shop.shop.controllers.users.models.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {
    @NotEmpty
    private String newEmail;
    private String password;
}
