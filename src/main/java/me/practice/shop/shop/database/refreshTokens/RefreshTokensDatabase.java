package me.practice.shop.shop.database.refreshTokens;

import me.practice.shop.shop.models.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokensDatabase extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByUsername(String username);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional()
    void deleteByUsername(String username);
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional()
    void deleteByExpireDateLessThan(Date date);
}
