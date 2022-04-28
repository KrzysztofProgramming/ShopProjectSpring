package me.practice.shop.shop.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Address {
    @NotBlank
    private String street;
    @Min(1)
    private Integer houseNumber;
    private Integer localNumber = -1;
    @NotBlank
    private String city;
    @Min(10000)
    @Max(99999)
    private Integer zipCode;
}
