package me.practice.shop.shop.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Builder
@NoArgsConstructor
@Data
@AllArgsConstructor
@Table(name = ShoppingCart.TABLE_NAME, indexes = @Index(name = "expire_date_idx", columnList = "expireDate"))
public class ShoppingCart {
    public static final String TABLE_NAME = "shopping_carts_table";

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "owner_username")
    private String ownerUsername;

    @ToString.Exclude
    @JsonIgnore()
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_username", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ShopUser owner;


    @ElementCollection
    @CollectionTable(name = "cart_products_ids",
            joinColumns = @JoinColumn(name = "cart_id", referencedColumnName = "owner_username"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "product_count")
    private Map<Long, Integer> items;
    private Date expireDate;

    public ShoppingCart(String ownerUsername, Map<Long, Integer> items, Date expireDate) {
        this.ownerUsername = ownerUsername;
        this.owner = ShopUser.builder().username(ownerUsername).build();
        this.items = items;
        this.expireDate = expireDate;
    }
}
