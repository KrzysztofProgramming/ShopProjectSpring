package me.practice.shop.shop.utils;


import org.springframework.data.domain.Sort;

public class OrdersSortUtils {
    public static final String DATE_ASC = "date_asc";
    public static final String DATE_DESC = "date_desc";
    public static final String PRICE_ASC = "price_asc";
    public static final String PRICE_DESC = "price_desc";

    public static Sort getSort(String name){
        return switch (name) {
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "totalPrice");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "totalPrice");
            case DATE_ASC -> Sort.by(Sort.Direction.ASC, "issuedDate");
            default -> Sort.by(Sort.Direction.DESC, "issuedDate");
        };
    }

    private OrdersSortUtils(){}
}
