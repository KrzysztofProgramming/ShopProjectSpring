package me.practice.shop.shop.utils;

import org.springframework.data.domain.Sort;

public class UsersSortUtils {
    public static final String ALPHABETIC_DESC = "alphabetic_desc";
    public static final String ALPHABETIC_ASC = "alphabetic_asc";
    public static final String ORDER_ASC = "order_asc";
    public static final String ORDER_DESC = "order_desc";

    private UsersSortUtils(){}

    public static Sort getSort(String name){
        return switch (name){
            case ALPHABETIC_ASC -> Sort.by(Sort.Direction.ASC, "username");
            case ALPHABETIC_DESC -> Sort.by(Sort.Direction.DESC, "username");
            case ORDER_ASC -> Sort.by(Sort.Direction.ASC, "order");
            case ORDER_DESC -> Sort.by(Sort.Direction.DESC, "order");
            default -> Sort.unsorted();
        };
    }

    public static boolean isEmpty(String sortName){
        return getSort(sortName).equals(Sort.unsorted());
    }
}
