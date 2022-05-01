package me.practice.shop.shop.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

import javax.persistence.*;
import java.util.Set;


@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = CommonType.TABLE_NAME,
        uniqueConstraints = @UniqueConstraint(name = "uk_type_name", columnNames = "name"),
        indexes = @Index(name = "index_name", columnList = "name"))
@Indexed
public class CommonType {
    public static final String TABLE_NAME = "types_table";

    @Id
    @SequenceGenerator(name="type_sequence", sequenceName = "type_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "type_sequence")
    @Column(name = "type_id")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    @KeywordField
    private String name;

    @ToString.Exclude
    @JsonIgnore
    @ManyToMany(mappedBy = "types", fetch = FetchType.LAZY)
    private Set<BookProduct> books;

    public CommonType(Long id, String name){
        this(id, name, null);
    }

    public CommonType(String name) {
        this.setName(name);
    }

    public void setName(String name) {
        this.name = toTypeName(name);
    }

    public static String toTypeName(String raw){
        if(raw.isEmpty()){
            return raw;
        }
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1).toLowerCase();
    }
}
