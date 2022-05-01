package me.practice.shop.shop.models;

import lombok.*;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;

import javax.persistence.*;
import java.util.Set;


@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = BookProduct.TABLE_NAME)
@Indexed
public class BookProduct {

    public static final String TABLE_NAME = "book_products_table";

    @Id
    @SequenceGenerator(sequenceName = "book_product_sequence", name = "book_product_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "book_product_sequence")
    @EqualsAndHashCode.Include
    @Column(name = "book_id")
    private Long id;

    @Column(nullable = false)
    @FullTextField
    private String name;

    @Column(nullable = false)
    @GenericField
    private Double price;

    @Column(length = 1000)
    @FullTextField()
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "books_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    @IndexedEmbedded()
    private Set<Author> authors;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "books_types",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "type_id")
    )
    @IndexedEmbedded
    private Set<CommonType> types;

    private Integer inStock;

}
