package me.practice.shop.shop.database.products;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.practice.shop.shop.models.BookProduct;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductQueryResult{

    @EqualsAndHashCode.Include
    private Long id;
    private Double price;
    private Integer inStock;
    private Boolean isArchived;
    private String name;
    private Long totalElements;
    private String description;

    public BookProduct toProduct(){
        return BookProduct.builder()
                .id(this.id)
                .price(this.price)
                .inStock(this.inStock)
                .isArchived(this.isArchived)
                .name(this.name)
                .build();
    }

    public ProductQueryResult(Object sqlResult){
        Object[] queryResult = (Object[]) sqlResult;
        this.id = ((BigInteger)queryResult[0]).longValue();
        this.price = (Double) queryResult[1];
        this.description = (String) queryResult[2];
        this.inStock = (Integer) queryResult[3];
        this.isArchived = (Boolean) queryResult[4];
        this.name = (String) queryResult[5];
        this.totalElements = ((BigInteger) queryResult[6]).longValue();
    }
}
