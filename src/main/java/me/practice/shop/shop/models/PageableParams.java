package me.practice.shop.shop.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageableParams {

    @Positive
    private Integer pageSize = 25;

    @Positive
    private Integer pageNumber = 1;
}
