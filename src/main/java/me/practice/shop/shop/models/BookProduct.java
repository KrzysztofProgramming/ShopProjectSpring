package me.practice.shop.shop.models;

import lombok.*;

import javax.persistence.*;
import java.util.Collection;


@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = BookProduct.TABLE_NAME)
public class BookProduct {

    public static final String TABLE_NAME = "book_products_table";

    @Id
    @SequenceGenerator(sequenceName = "book_product_sequence", name = "book_product_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "book_product_sequence")
    @EqualsAndHashCode.Include
    @Column(name = "book_id")
    private Long id;

    private String name;

    private Double price;

    private String description;

    @ManyToMany(cascade = {CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "books_authors",
            joinColumns = @JoinColumn(name = "fk_book"),
            inverseJoinColumns = @JoinColumn(name = "fk_author")
    )
    private Collection<Author> authors;

    private Collection<CommonType> types;

    private Integer inStock;

}
