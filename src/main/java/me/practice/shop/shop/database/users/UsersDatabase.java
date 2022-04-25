package me.practice.shop.shop.database.users;

import me.practice.shop.shop.models.ShopUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersDatabase extends JpaRepository<ShopUser, String> {
    Optional<ShopUser> findByEmail(String email);
    boolean existsByEmail(String email);

}
