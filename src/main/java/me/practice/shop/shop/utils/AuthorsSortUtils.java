package me.practice.shop.shop.utils;

import lombok.Data;

@Data
public class AuthorsSortUtils {
    public static final String ALPHABETIC_ASC = "alph_asc";
    public static final String ALPHABETIC_DESC = "alph_desc";
    public static final String WRITTEN_BOOKS_ASC = "books_asc";
    public static final String WRITTEN_BOOKS_DESC = "books_desc";

    public static String getSort(String code){
        return switch (code){
            case ALPHABETIC_ASC -> " ORDER BY a.name ASC";
            case ALPHABETIC_DESC -> " ORDER BY a.name DESC";
            case WRITTEN_BOOKS_ASC -> " ORDER BY COUNT(b) ASC";
            case WRITTEN_BOOKS_DESC -> " ORDER BY COUNT(b) DESC";
            default -> " ORDER BY a.id ASC";
        };
    }

    private AuthorsSortUtils(){}
}
