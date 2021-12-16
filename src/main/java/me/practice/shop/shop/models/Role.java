package me.practice.shop.shop.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;

@Data
@AllArgsConstructor
@Document("roles")
public class Role {
    @Id
    private String name;
    private Collection<String> permissions;

    @Override
    public String toString(){
        return name;
    }
}
