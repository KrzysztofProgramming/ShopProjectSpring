package me.practice.shop.shop.models;

import lombok.*;

import javax.persistence.*;
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

    @ElementCollection
    @CollectionTable(name = "cart_products_ids",
            joinColumns = @JoinColumn(name = "cart_id", referencedColumnName = "ownerUsername"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "product_count")
    private Map<Long, Integer> items;
    private Date expireDate;
}
