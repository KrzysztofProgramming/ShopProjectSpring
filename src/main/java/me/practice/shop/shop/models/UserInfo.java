package me.practice.shop.shop.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class UserInfo {
    @NotBlank
    private String firstname;
    @NotBlank
    private String lastname;
    @Min(100000000)
    @Max(999999999)
    private long phoneNumber;
    @NotNull
    @Valid
    @Embedded
    private Address address;
}
