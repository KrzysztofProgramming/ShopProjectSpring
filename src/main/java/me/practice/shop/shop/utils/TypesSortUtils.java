package me.practice.shop.shop.utils;

import org.apache.logging.log4j.util.Strings;

public class TypesSortUtils {
    public static final String ALPHABETIC_ASC = "alph_asc";
    public static final String ALPHABETIC_DESC = "alph_desc";
    public static final String WRITTEN_BOOKS_ASC = "books_asc";
    public static final String WRITTEN_BOOKS_DESC = "books_desc";

    private TypesSortUtils() {}

    public static String getSort(String code){
        if(Strings.isEmpty(code)) return " ORDER BY t.name ASC";
        return switch (code){
            case ALPHABETIC_DESC -> " ORDER BY t.name DESC";
            case WRITTEN_BOOKS_ASC -> " ORDER BY COUNT(t) ASC";
            case WRITTEN_BOOKS_DESC -> " ORDER BY COUNT(t) DESC";
            default -> " ORDER BY t.name ASC";
        };
    }
}
