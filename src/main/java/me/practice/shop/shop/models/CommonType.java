package me.practice.shop.shop.models;

import lombok.*;
import org.hibernate.annotations.Formula;
import org.springframework.data.annotation.Id;

import javax.persistence.*;


@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = CommonType.TABLE_NAME,
        uniqueConstraints = @UniqueConstraint(name = "uk_type_name", columnNames = "name"),
        indexes = @Index(name = "index_name", columnList = "name"))
public class CommonType {
    public static final String TABLE_NAME = "type_table";

    @Id
    @SequenceGenerator(name="type_sequence", sequenceName = "type_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "type_sequence")
    @Column(name = "type_id")
    @EqualsAndHashCode.Include
    private Long id;

    private String name;

    @Basic(fetch = FetchType.LAZY)
    @Formula(value = "(SELECT COUNT(*)) FROM books_types bt WHERE bt.fk_type = type_id")
    private Integer productsCount = null;

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
