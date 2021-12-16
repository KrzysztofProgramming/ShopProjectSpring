package me.practice.shop.shop.controllers.perms.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {
    @NotEmpty
    private String name;

    @NotNull
    private Collection<String> authorities;
}
