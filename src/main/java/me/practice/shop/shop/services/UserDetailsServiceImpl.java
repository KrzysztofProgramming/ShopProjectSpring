package me.practice.shop.shop.services;

import me.practice.shop.shop.database.users.UsersDatabase;
import me.practice.shop.shop.models.ShopUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsersDatabase usersDatabase;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ShopUser user = getUserByUsername(username);
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(this.fuseRolesWithAuthorities(user))
                .build();
    }

    public ShopUser getUserByUsername(String username) throws UsernameNotFoundException{
        return usersDatabase.findById(username).orElseThrow(()
                -> new UsernameNotFoundException("User not exists"));
    }

    private Collection<GrantedAuthority> fuseRolesWithAuthorities(ShopUser user){
        return Stream.of(user.getAuthorities().stream(), user.getRoles().stream().map(role->"ROLE_" + role))
                .flatMap(Function.identity()).map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
