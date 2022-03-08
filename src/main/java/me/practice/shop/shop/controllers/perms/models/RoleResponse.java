package me.practice.shop.shop.controllers.perms.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.practice.shop.shop.models.Role;
import me.practice.shop.shop.permissions.Permissions;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RoleResponse {
    @EqualsAndHashCode.Include
    private String name;
    private long authorities;
    private double order;

    public RoleResponse(Role role){
        this.name = role.getName();
        this.authorities = Permissions.toNumber(role.getAuthorities());
        this.order = role.getStrength();
    }
}
