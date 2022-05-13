package me.practice.shop.shop.controllers.users.models.profile;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.practice.shop.shop.models.PageableParams;
import me.practice.shop.shop.utils.OrdersSortUtils;

import javax.validation.constraints.PositiveOrZero;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class GetOrdersParams extends PageableParams {
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'hh:mm:ss.SSS'Z'";

    @PositiveOrZero
    private Double maxPrice = null;
    @PositiveOrZero
    private Double minPrice = null;
    private Date maxDate = null;
    private Date minDate = null;
    private Integer status = null;
    private String sort = OrdersSortUtils.DATE_DESC;

    public void setMaxDate(String maxDate) throws ParseException {
        DateFormat format = new SimpleDateFormat(DATE_FORMAT);
        this.maxDate = format.parse(maxDate);
    }

    public void setMinDate(String minDate) throws ParseException {
        DateFormat format = new SimpleDateFormat(DATE_FORMAT);
        this.minDate = format.parse(minDate);;
    }
}
