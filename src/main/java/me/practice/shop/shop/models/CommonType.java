package me.practice.shop.shop.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import javax.persistence.*;


@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = CommonType.TABLE_NAME,
        uniqueConstraints = @UniqueConstraint(name = "uk_type_name", columnNames = "name"))
public class CommonType {
    public static final String TABLE_NAME = "type_table";

    @Id
    @SequenceGenerator(name="type_sequence", sequenceName = "type_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "type_sequence")
    @EqualsAndHashCode.Include
    private Long id;

    private String name;

    @Transient
    private Integer productsCount = null;

    public CommonType(String name, int productsCount) {
        this.productsCount = productsCount;
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
