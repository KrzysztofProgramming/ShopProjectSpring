package me.practice.shop.shop.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    @NotBlank
    private String street;
    @Min(1)
    private int houseNumber;
    private int localNumber = -1;
    @NotBlank
    private String city;
    @Min(10000)
    @Max(99999)
    private int zipCode;
}
