package me.practice.shop.shop.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.practice.shop.shop.permissions.Permissions;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document(ShopUser.COLLECTION_NAME)
public class ShopUser {

    public static final String COLLECTION_NAME = "users_database";

    @Id
    @EqualsAndHashCode.Include
    private String username;
    @Indexed(unique = true)
    private String email;
    private String password;

    @DBRef
    private Collection<Role> roles;

    public Collection<String> getRolesStrings(){
        return roles.stream().map(Role::toString).collect(Collectors.toList());
    }

    //getMergesAuthorities roles + additionalAuthorities
    public Collection<String> getAuthorities(){
        return roles.stream().flatMap(role->role.getAuthorities().stream()).collect(Collectors.toSet());
    }

//    private Collection<String> getRolesAuthorities(){
//        return roles.stream().map(role -> Roles.fromString(role).getAuthorities())
//                .flatMap(Collection::stream).collect(Collectors.toList());
//    }

    public long getAuthoritiesNumber(){
        return Permissions.toNumber(getAuthorities());
    }

}
