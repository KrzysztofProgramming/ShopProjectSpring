package me.practice.shop.shop.services;

import me.practice.shop.shop.database.users.RolesRepository;
import me.practice.shop.shop.database.users.UsersRepository;
import me.practice.shop.shop.models.Role;
import me.practice.shop.shop.models.ShopUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RolesRepository rolesRepository;

    private final PasswordEncoder encoder = new Argon2PasswordEncoder();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        ShopUser user = username.equals("admin") ? new ShopUser("admin", "admin@gmail.com",
//                encoder.encode("admin"), List.of(new Role("admin",
//                Permissions.fromNumber(0b1111111111111).stream().map(Permissions::toString)
//                        .collect(Collectors.toList())))) : getUserByUsername(username);
        ShopUser user = getUserByUsername(username);
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(this.fusedRolesWithAuthorities(user))
                .build();
    }

    public ShopUser getUserByUsername(String username) throws UsernameNotFoundException{
        return usersRepository.findById(username).orElseThrow(()
                -> new UsernameNotFoundException("User not exists"));
    }

    public Set<Role> getRolesByNames(Iterable<String> names) throws IllegalArgumentException{
        List<Role> list = rolesRepository.findAllById(names);
        return new HashSet<>(list);
    }

    private Collection<GrantedAuthority> fusedRolesWithAuthorities(ShopUser user){
        return Stream.of(user.getAuthorities().stream(), user.getRoles().stream().map(role->"ROLE_" + role))
                .flatMap(Function.identity()).map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
