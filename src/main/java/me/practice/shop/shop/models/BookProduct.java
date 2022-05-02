package me.practice.shop.shop.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import me.practice.shop.shop.database.files.DatabaseImage;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;

import javax.persistence.*;
import java.util.Set;


@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = BookProduct.TABLE_NAME, indexes = @Index(name = "index_product_archived", columnList = "isArchived"))
@Indexed
public class BookProduct {

    public static final String TABLE_NAME = "book_products_table";

    @Id
    @SequenceGenerator(sequenceName = "book_product_sequence", name = "book_product_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "book_product_sequence")
    @EqualsAndHashCode.Include
    @Column(name = "book_id")
    @GenericField(sortable = Sortable.YES)
    private Long id;

    @Column(nullable = false)
    @FullTextField()
    @KeywordField(sortable = Sortable.YES, name = "name_sort")
    private String name;

    @Column(nullable = false)
    @GenericField(sortable = Sortable.YES)
    private Double price;

    @Column(length = 1000)
    @FullTextField()
    private String description;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product")
    @JsonIgnore
    @ToString.Exclude
    private Set<DatabaseImage> images;

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

    @GenericField
    private Integer inStock;

    @GenericField
    private Boolean isArchived = false; //TODO implement it

}
