package me.practice.shop.shop.database.users;

import lombok.extern.slf4j.Slf4j;
import me.practice.shop.shop.models.Role;
import me.practice.shop.shop.models.ShopUser;
import me.practice.shop.shop.permissions.Permissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.Set;

@Component
@Slf4j
public class AdminUserInitialization {

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private ApplicationArguments arguments;

    @PostConstruct
    private void createAdminUserIfNotExists(){
        if(!(this.arguments.containsOption("create-admin") &&
                this.arguments.getOptionValues("create-admin").get(0).equals("true")))
            return;
        log.info("Checking for admin user creation");
        Optional<ShopUser> user = this.usersRepository.findById("admin");
        if(user.isPresent()) {
            log.warn("Admin user already exists");
            return;
        }
        ShopUser adminUser = ShopUser.builder().username("admin")
                .email("admin")
                .password(encoder.encode("admin"))
                .roles(Set.of(this.createAdminRoleIfNotExists())).build();
        this.usersRepository.save(adminUser);
        log.info("Admin user has been created successfully with default \"admin\" password.");
        System.out.println(adminUser);
    }

    private Role createAdminRoleIfNotExists(){
        Optional<Role> adminRole = this.rolesRepository.findByStrength(0.0);
        if(adminRole.isPresent()) return adminRole.get();
        adminRole = this.rolesRepository.findByName("admin");
        return adminRole.orElseGet(() -> this.rolesRepository.save(Role.builder().name("admin").strength(0.0)
                .authorities(Permissions.fromNumber(Permissions.allPerms())).build()));
    }
}
