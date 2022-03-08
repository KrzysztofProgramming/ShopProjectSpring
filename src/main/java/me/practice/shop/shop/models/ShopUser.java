package me.practice.shop.shop.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.practice.shop.shop.permissions.Permissions;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TextScore;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document(ShopUser.COLLECTION_NAME)
public class ShopUser {

    public static final String COLLECTION_NAME = "users_database";

    @Id
    @EqualsAndHashCode.Include
    @TextIndexed
    private String username;
    @Indexed(unique = true)
    private String email;
    private String password;
    @DBRef
    private Collection<Role> roles;
    private UserInfo userInfo;

    @TextScore
    private float textScore;

    public Collection<String> getRolesStrings(){
        return roles.stream().map(Role::toString).collect(Collectors.toList());
    }

    //getMergesAuthorities roles + additionalAuthorities
    public Collection<String> getAuthorities(){
        return roles.stream().flatMap(role->role.getAuthorities().stream()).collect(Collectors.toSet());
    }

    public Optional<Role> getHighestRole(){
        if(this.roles.isEmpty()) return Optional.empty();
        return Optional.of(this.roles.stream().reduce(this.roles.stream().findAny().get(),
                (role, role2) -> role.getStrength() > role2.getStrength() ? role2 : role));
    }

//    private Collection<String> getRolesAuthorities(){
//        return roles.stream().map(role -> Roles.fromString(role).getAuthorities())
//                .flatMap(Collection::stream).collect(Collectors.toList());
//    }

    public long getAuthoritiesNumber(){
        return Permissions.toNumber(getAuthorities());
    }

    public ShopUser(String username, String email, String password, Collection<Role> roles, UserInfo userInfo) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.userInfo = userInfo;
    }
}
