package me.practice.shop.shop.models;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;
import java.util.Map;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Builder
@NoArgsConstructor
@Data
@AllArgsConstructor
@Table(name = ShoppingCart.TABLE_NAME, indexes = @Index(name = "index_expire_date", columnList = "expireDate"))
public class ShoppingCart {
    public static final String TABLE_NAME = "shopping_cars_table";

    @Id
    @EqualsAndHashCode.Include
    private String ownerUsername;

    private Map<String, Integer> items;
    private Date expireDate;
}
