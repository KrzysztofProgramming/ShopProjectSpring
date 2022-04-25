package me.practice.shop.shop.models;

import lombok.*;
import org.hibernate.annotations.Formula;

import javax.persistence.*;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = Author.TABLE_NAME, uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "uk_author_name"))
public class Author {
    public final static String TABLE_NAME = "authors_table";

    @Id
    @SequenceGenerator(name = "author_sequence", sequenceName = "author_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "author_sequence")
    @Column(name = "author_id")
    private Long id;

    private String name;

    private String description;

//    @Basic(fetch = FetchType.LAZY) TODO check this
    @Formula(value = "(SELECT COUNT(*) FROM books_authors ba WHERE ba.fk_author = author_id)")
    private Integer writtenBooks;

    public Author(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
