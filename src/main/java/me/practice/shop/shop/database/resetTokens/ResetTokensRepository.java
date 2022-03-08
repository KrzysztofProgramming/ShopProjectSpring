package me.practice.shop.shop.database.resetTokens;

import me.practice.shop.shop.models.ResetPasswordToken;
import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface ResetTokensRepository extends MongoRepository<ResetPasswordToken, String> {

    @Query("{ownerUsername: ?0}")
    Optional<ResetPasswordToken> findByUsername(String username);

    @DeleteQuery(value = "{ownerUsername: ?0}")
    void deleteByUsername(String username);

    void deleteByExpireDateLessThan(Date date);
}
