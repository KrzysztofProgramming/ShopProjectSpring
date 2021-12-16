package me.practice.shop.shop.services;

import me.practice.shop.shop.database.users.RolesRepository;
import me.practice.shop.shop.database.users.UsersDatabase;
import me.practice.shop.shop.models.Role;
import me.practice.shop.shop.models.ShopUser;
import me.practice.shop.shop.utils.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsersDatabase usersDatabase;

    @Autowired
    private RolesRepository rolesRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ShopUser user = getUserByUsername(username);
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(this.fusedRolesWithAuthorities(user))
                .build();
    }

    public ShopUser getUserByUsername(String username) throws UsernameNotFoundException{
        return usersDatabase.findById(username).orElseThrow(()
                -> new UsernameNotFoundException("User not exists"));
    }

    public Collection<Role> getRolesByNames(Iterable<String> names) throws IllegalArgumentException{

        List<Role> list = IterableUtils.toList(rolesRepository.findAllById(names));
        if(list.size() != IterableUtils.size(names))
            throw new IllegalArgumentException();
        return list;
    }

    private Collection<GrantedAuthority> fusedRolesWithAuthorities(ShopUser user){
        return Stream.of(user.getAuthorities().stream(), user.getRoles().stream().map(role->"ROLE_" + role))
                .flatMap(Function.identity()).map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
