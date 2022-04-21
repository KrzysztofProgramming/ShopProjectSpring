package me.practice.shop.shop.models;

import lombok.*;
import me.practice.shop.shop.permissions.Permissions;

import javax.persistence.*;
import java.util.Collection;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@NoArgsConstructor
@Builder
@Table(name = Role.TABLE_NAME, indexes = @Index(name = "index_strength", columnList = "strength"))
public class Role {
    public static final String TABLE_NAME = "roles_table";

    @Id
    @EqualsAndHashCode.Include
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "role_authorities",
            joinColumns = @JoinColumn(name="role_name", referencedColumnName = "name"))
    @Column(name="authority")
    private Collection<String> authorities;
    private double strength;

    public Collection<String> getAuthorities() {
        return this.strength == 0 ? Permissions.fromNumber(Permissions.allPerms()) : authorities;
    }

    @Override
    public String toString(){
        return name;
    }
}
