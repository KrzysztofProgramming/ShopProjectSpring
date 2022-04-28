package me.practice.shop.shop.models;

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
@Table(name = ShopOrder.TABLE_NAME, indexes = @Index(name = "index_order_email", columnList = "email"))
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
    private String ownerUsername;

    @Column(nullable = false)
    private String email;
    @Embedded
    private UserInfo info;

    @ElementCollection()
    @CollectionTable(name = "order_products_ids",
            joinColumns = @JoinColumn(name = "fk_order", referencedColumnName = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "product_count")
    private Map<Long, Integer> productsIds;
    private Date issuedDate;
    private Double totalPrice;
    private Integer status;

    public ShopOrder(String ownerUsername, String email, UserInfo info, Map<Long, Integer> productsIds, Date issuedDate, Double totalPrice, Integer status) {
        this.ownerUsername = ownerUsername;
        this.email = email;
        this.info = info;
        this.productsIds = productsIds;
        this.issuedDate = issuedDate;
        this.totalPrice = totalPrice;
        this.status = status;
    }
}
