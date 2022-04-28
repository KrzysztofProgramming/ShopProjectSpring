package me.practice.shop.shop.database.resetTokens;

import me.practice.shop.shop.models.ResetPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResetTokensRepository extends JpaRepository<ResetPasswordToken, UUID> {

    Optional<ResetPasswordToken> findByOwnerUsername(String username);

    void deleteByOwnerUsername(String username);

    void deleteByExpireDateLessThan(Date date);
}
