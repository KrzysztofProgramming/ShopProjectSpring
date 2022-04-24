package me.practice.shop.shop.database.users;

import me.practice.shop.shop.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolesRepository extends JpaRepository<Role, String> {

    Optional<Role> findByStrength(Double strength);

    Optional<Role> findByName(String name);
}
