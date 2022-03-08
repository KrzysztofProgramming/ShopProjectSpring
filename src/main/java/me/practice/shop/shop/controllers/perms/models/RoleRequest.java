package me.practice.shop.shop.controllers.perms.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {
    @NotEmpty
    private String name;

    @Min(0)
    private double order = -1;

    @Min(0)
    private long authorities = -1;

    public void setName(String name) {
        this.name = name.toLowerCase();
    }
}
