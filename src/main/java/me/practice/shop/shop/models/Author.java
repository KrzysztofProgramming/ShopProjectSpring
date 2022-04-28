package me.practice.shop.shop.models;

import lombok.*;

import javax.persistence.*;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = Author.TABLE_NAME, uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "uk_author_name"),
indexes = @Index(columnList = "name", name = "index_author_name"))
public class Author {
    public final static String TABLE_NAME = "authors_table";

    @Id
    @SequenceGenerator(name = "author_sequence", sequenceName = "author_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "author_sequence")
    @Column(name = "author_id")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;
}
