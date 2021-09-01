package me.practice.shop.shop.database.tokens;

import me.practice.shop.shop.models.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface RefreshTokensDatabase extends MongoRepository<RefreshToken, String> {
    Optional<RefreshToken> findByUsername(String username);
    void deleteByUsername(String username);
    void deleteByExpireDateLessThan(Date date);
}
