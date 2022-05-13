package me.practice.shop.shop.utils;


import org.apache.logging.log4j.util.Strings;

public class OrdersSortUtils {
    public static final String DATE_ASC = "date_asc";
    public static final String DATE_DESC = "date_desc";
    public static final String PRICE_ASC = "price_asc";
    public static final String PRICE_DESC = "price_desc";

    public static String getSort(String name){
        if(Strings.isEmpty(name)) return " ORDER BY o.issuedDate DESC";
        return switch (name) {
            case PRICE_ASC -> " ORDER BY o.totalPrice ASC";
            case PRICE_DESC -> " ORDER BY o.totalPrice DESC";
            case DATE_ASC -> " ORDER BY o.issuedDate ASC";
            default -> " ORDER BY o.issuedDate DESC";
        };
    }

    private OrdersSortUtils(){}
}
