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

    public static String getSortString(String name){
        if(name==null) return " ORDER BY b.book_id ASC";
        return switch (name) {
            case PRICE_ASC -> " ORDER BY b.price ASC";
            case PRICE_DESC -> " ORDER BY b.price DESC";
            case ALPHABETIC_ASC -> " ORDER BY b.name ASC";
            case ALPHABETIC_DESC -> " ORDER BY b.name DESC";
            case ID_DESC -> " ORDER BY b.book_id DESC";
            default -> " ORDER BY b.book_id ASC";
        };
    }

    public static Sort getSort(String name){
        if(name==null) return Sort.by("id").ascending();
        return switch (name) {
            case PRICE_ASC -> Sort.by("price").ascending();
            case PRICE_DESC -> Sort.by("price").descending();
            case ALPHABETIC_ASC -> Sort.by("name").ascending();
            case ALPHABETIC_DESC -> Sort.by("name").descending();
            case ID_DESC -> Sort.by("id").descending();
            default -> Sort.by("id").ascending();
        };
    }
}
