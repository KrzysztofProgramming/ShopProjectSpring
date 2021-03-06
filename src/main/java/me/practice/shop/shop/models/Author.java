package me.practice.shop.shop.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = Author.TABLE_NAME, uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "uk_author_name"),
indexes = @Index(columnList = "name", name = "author_name_idx"))
public class Author {
    public final static String TABLE_NAME = "authors_table";

    @Id
    @SequenceGenerator(name = "author_sequence", sequenceName = "author_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "author_sequence")
    @Column(name = "author_id")
    @EqualsAndHashCode.Include
    private Long id;

    @ToString.Exclude
    @JsonIgnore
    @ManyToMany(mappedBy = "authors", fetch = FetchType.LAZY)
    Set<BookProduct> books;

    @Column(nullable = false)
    private String name;

    private String description;

    public Author(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
