package me.practice.shop.shop.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import me.practice.shop.shop.database.files.DatabaseImage;

import javax.persistence.*;
import java.util.Set;


@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = BookProduct.TABLE_NAME, indexes =
        {
                @Index(name = "product_archived_idx", columnList = "isArchived"),
                @Index(name = "product_price_idx", columnList = "price"),
                @Index(name = "product_in_stock_idx", columnList = "inStock")
        })
public class BookProduct {

    public static final String TABLE_NAME = "book_products_table";

    @Id
    @SequenceGenerator(sequenceName = "book_product_sequence", name = "book_product_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "book_product_sequence")
    @EqualsAndHashCode.Include
    @Column(name = "book_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price;

    @Column(length = 1000)
    private String description;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product")
    @JsonIgnore
    @ToString.Exclude
    private Set<DatabaseImage> images;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "books_authors",
            joinColumns = @JoinColumn(name = "book_id", foreignKey = @ForeignKey(foreignKeyDefinition =
                    "FOREIGN KEY (book_id) REFERENCES book_products_table (book_id) ON DELETE CASCADE")),
            inverseJoinColumns = @JoinColumn(name = "author_id", foreignKey = @ForeignKey(foreignKeyDefinition =
                    "FOREIGN KEY (author_id) REFERENCES authors_table (author_id) ON DELETE CASCADE"))
    )
    private Set<Author> authors;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "books_types",
            joinColumns = @JoinColumn(name = "book_id", foreignKey = @ForeignKey(foreignKeyDefinition =
                    "FOREIGN KEY (book_id) REFERENCES book_products_table (book_id) ON DELETE CASCADE")),
            inverseJoinColumns = @JoinColumn(name = "type_id", foreignKey = @ForeignKey(foreignKeyDefinition =
                    "FOREIGN KEY (type_id) REFERENCES types_table (type_id) ON DELETE CASCADE"))
    )
    private Set<CommonType> types;

    private Integer inStock;

    @Builder.Default
    private Boolean isArchived = false; //TODO implement it

}
