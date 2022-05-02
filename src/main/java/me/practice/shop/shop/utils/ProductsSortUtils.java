package me.practice.shop.shop.utils;

import org.hibernate.search.engine.search.sort.dsl.SearchSortFactory;
import org.hibernate.search.engine.search.sort.dsl.SortFinalStep;

public class ProductsSortUtils {
    public static final String PRICE_ASC = "price_asc";
    public static final String PRICE_DESC = "price_desc";
    public static final String ALPHABETIC_ASC = "alphabetic_asc";
    public static final String ALPHABETIC_DESC = "alphabetic_desc";
    public static final String ID_ASC = "id_asc";
    public static final String ID_DESC = "id_desc";

    private ProductsSortUtils() {}

    public static SortFinalStep getSort(SearchSortFactory f, String name){
        return switch (name) {
            case PRICE_ASC -> f.field("price").asc(); // Sort.by(Sort.Direction.ASC, "price");
            case PRICE_DESC -> f.field("price").desc();
            case ALPHABETIC_ASC -> f.field("name_sort").asc();
            case ALPHABETIC_DESC -> f.field("name_sort").desc();
            case ID_ASC -> f.field("id").asc();
            case ID_DESC -> f.field("id").desc();
            default -> f.score().asc();
        };
    }

}
