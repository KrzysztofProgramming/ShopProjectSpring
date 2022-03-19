package me.practice.shop.shop.controllers.authors.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.practice.shop.shop.models.PageableParams;

import javax.validation.constraints.Min;

@Data()
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class GetAuthorsParams extends PageableParams {
    private String searchPhrase = "";
    @Min(0)
    private Integer minBooks = null;
    @Min(0)
    private Integer maxBooks = null;
}
