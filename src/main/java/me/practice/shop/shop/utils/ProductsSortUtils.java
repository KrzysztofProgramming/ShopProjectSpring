package me.practice.shop.shop.utils;

import org.springframework.data.domain.Sort;

public class ProductsSortUtils {
    public static final String PRICE_ASC = "price_asc";
    public static final String PRICE_DESC = "price_desc";
    public static final String ALPHABETIC_ASC = "alphabetic_asc";
    public static final String ALPHABETIC_DESC = "alphabetic_desc";

    private ProductsSortUtils() {}

    public static Sort getSort(String name){
        switch(name){
            case PRICE_ASC:
                return Sort.by(Sort.Direction.ASC, "price");
            case PRICE_DESC:
                return Sort.by(Sort.Direction.DESC, "price");
            case ALPHABETIC_ASC:
                return Sort.by(Sort.Direction.ASC, "name");
            case ALPHABETIC_DESC:
                return Sort.by(Sort.Direction.DESC, "name");
            default: return Sort.unsorted();
        }
    }

    public static boolean isEmpty(String sortName){
        return getSort(sortName).equals(Sort.unsorted());
    }

}
