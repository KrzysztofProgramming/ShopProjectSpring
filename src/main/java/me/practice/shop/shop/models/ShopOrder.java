package me.practice.shop.shop.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;

@Data
@Document("orders")
@AllArgsConstructor
public class ShopOrder {
    public static final int PAID = 1;
    public static final int CANCELLED = 2;
    public static final int UNPAID = 0;
    public static final int UNKNOWN = -1;
    public static final String COLLECTION_NAME = "orders";

    @Id
    private String id;
    @Indexed
    private String ownerUsername;
    @Indexed
    private String email;
    private UserInfo info;
    private Map<String, Integer> products;
    private Date issuedDate;
    private double totalPrice;
    @Indexed
    private int status;
}
