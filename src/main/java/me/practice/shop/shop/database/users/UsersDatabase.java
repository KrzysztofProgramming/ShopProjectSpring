package me.practice.shop.shop.database.users;

import me.practice.shop.shop.models.ShopUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersDatabase extends MongoRepository<ShopUser, String>, UsersSearcher {
    Optional<ShopUser> findByEmail(String email);
    boolean existsByEmail(String email);
}
