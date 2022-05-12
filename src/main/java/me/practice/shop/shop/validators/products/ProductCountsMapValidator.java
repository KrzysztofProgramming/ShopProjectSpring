package me.practice.shop.shop.validators.products;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Map;

public class ProductCountsMapValidator implements ConstraintValidator<ProductsCountsMap, Map<Long, Integer>> {

    private int maxValue;
    private int minValue;

    @Override
    public void initialize(ProductsCountsMap constraintAnnotation) {
        this.maxValue = constraintAnnotation.maxValue();
        this.minValue = constraintAnnotation.minValue();
    }

    @Override
    public boolean isValid(Map<Long, Integer> toCheck, ConstraintValidatorContext context) {
        return toCheck.values().stream().allMatch(value->value >= minValue && value <= maxValue);
    }
}
