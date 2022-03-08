package me.practice.shop.shop.controllers.users.models.profile;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.practice.shop.shop.models.PageableParams;

import javax.validation.constraints.PositiveOrZero;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class GetOrdersParams extends PageableParams {
    @PositiveOrZero
    private Double maxPrice = null;
    @PositiveOrZero
    private Double minPrice = null;
    private Date maxDate;
    private Date minDate;
    private Integer status = null;
}
