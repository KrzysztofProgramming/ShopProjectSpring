package me.practice.shop.shop.validators.products;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ProductCountsMapValidator.class)
@Target( { ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ProductsCountsMap {
    String message() default "Invalid character in username";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int minValue() default 1;
    int maxValue() default Integer.MAX_VALUE;

}
