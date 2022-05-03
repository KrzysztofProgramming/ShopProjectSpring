package me.practice.shop.shop.utils;

import org.springframework.data.domain.Sort;

public class ProductsSortUtils {
    public static final String PRICE_ASC = "price_asc";
    public static final String PRICE_DESC = "price_desc";
    public static final String ALPHABETIC_ASC = "alphabetic_asc";
    public static final String ALPHABETIC_DESC = "alphabetic_desc";
    public static final String ID_ASC = "id_asc";
    public static final String ID_DESC = "id_desc";

    private ProductsSortUtils() {}

    public static Sort getSort(String name){
        if(name==null) return Sort.unsorted();
        return switch (name) {
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "price");
            case ALPHABETIC_ASC -> Sort.by(Sort.Direction.ASC, "name");
            case ALPHABETIC_DESC -> Sort.by(Sort.Direction.DESC, "name");
            default -> Sort.unsorted();
        };
    }

    public static boolean isEmpty(String sortName){
        return getSort(sortName).equals(Sort.unsorted());
    }

}
