package me.practice.shop.shop.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.practice.shop.shop.permissions.Permissions;
import me.practice.shop.shop.permissions.Roles;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@Document("user_database")
public class ShopUser {
    @Id
    private String username;
    private String email;
    private String password;
    private Collection<String> roles;
    private Collection<String> additionalAuthorities;


    //getMergesAuthorities roles + additionalAuthorities
    public Collection<String> getAuthorities(){
        return Stream.of(additionalAuthorities, getRolesAuthorities())
                .flatMap(Collection::stream).collect(Collectors.toSet());
    }

    private Collection<String> getRolesAuthorities(){
        return roles.stream().map(role -> Roles.fromString(role).getAuthorities())
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    public long getAuthoritiesNumber(){
        return Permissions.toNumber(getAuthorities());
    }

}
