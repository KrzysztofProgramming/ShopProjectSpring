package me.practice.shop.shop.controllers.products.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveRequest {
    @NotNull
    private Boolean archive;
}
