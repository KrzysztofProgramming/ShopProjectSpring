package me.practice.shop.shop.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = ShopOrder.TABLE_NAME, indexes = {
        @Index(name = "order_email_idx", columnList = "email"),
        @Index(name = "order_owner_idx", columnList = "owner_username"),
        @Index(name = "order_date_idx", columnList = "issuedDate"),
        @Index(name = "order_total_price_idx", columnList = "totalPrice"),
        @Index(name = "order_status_idx", columnList = "status")
})
public class ShopOrder {
    public static final int PAID = 1;
    public static final int CANCELLED = 2;
    public static final int UNPAID = 0;
    public static final int UNKNOWN = -1;
    public static final String TABLE_NAME = "orders_table";

    @Id
    @SequenceGenerator(sequenceName = "order_sequence", name = "order_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_sequence")
    @Column(name = "order_id")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name="owner_username")
    private String ownerUsername;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_username", insertable = false, updatable = false)
    @ToString.Exclude
    @JsonIgnore
    private ShopUser owner;

    @Column(nullable = false)
    private String email;
    @Embedded
    private UserInfo info;

    @ElementCollection()
    @CollectionTable(name = "order_products_ids",
            joinColumns = @JoinColumn(name = "order_id", referencedColumnName = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "product_count")
    private Map<Long, Integer> productsIds;
    private Date issuedDate;
    private Double totalPrice;
    private Integer status;

    public void setOwner(ShopUser owner) {
        this.owner = owner;
        this.ownerUsername = owner.getUsername();
    }

    public void setOwnerUsername(String ownerUsername){
        this.ownerUsername = ownerUsername;
        this.owner = ShopUser.builder().username(ownerUsername).build();
    }

    public ShopOrder(String ownerUsername, String email, UserInfo info, Map<Long, Integer> productsIds, Date issuedDate, Double totalPrice, Integer status) {
        this.ownerUsername = ownerUsername;
        this.owner = ShopUser.builder().username(ownerUsername).build();
        this.email = email;
        this.info = info;
        this.productsIds = productsIds;
        this.issuedDate = issuedDate;
        this.totalPrice = totalPrice;
        this.status = status;
    }
}
