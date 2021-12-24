package me.practice.shop.shop.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document("roles")
public class Role {
    @Id
    @EqualsAndHashCode.Include
    private String name;
    private Collection<String> authorities;

    @Override
    public String toString(){
        return name;
    }
}
