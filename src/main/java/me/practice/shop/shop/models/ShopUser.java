package me.practice.shop.shop.models;

import lombok.*;
import me.practice.shop.shop.permissions.Permissions;

import javax.persistence.*;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = ShopUser.TABLE_NAME, indexes = {
        @Index(name = "index_email", columnList = "email")
})
public class ShopUser {

    public static final String TABLE_NAME = "users_table";

    @Id
    @EqualsAndHashCode.Include
    private String username;
    private String email;
    private String password;
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "fk_user"),
            inverseJoinColumns = @JoinColumn(name="fk_role")
    )
    private Collection<Role> roles;
    @Embedded
    private UserInfo userInfo;

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
}
