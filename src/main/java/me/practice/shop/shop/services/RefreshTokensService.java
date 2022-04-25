package me.practice.shop.shop.services;

import me.practice.shop.shop.database.refreshTokens.RefreshTokensDatabase;
import me.practice.shop.shop.models.RefreshToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokensService {
    @Value("${application.jwt.refreshing.expiration}")
    private long expirationTime; //in Days

    @Autowired
    private RefreshTokensDatabase refreshTokensDatabase;

    public RefreshToken newRefreshToken(String username){
        refreshTokensDatabase.deleteByUsername(username);
        return refreshTokensDatabase.save(new RefreshToken(UUID.randomUUID().toString(),
                username, new Date(), calcExpireDate()));
    }

    public RefreshToken createNewTokenOrRenew(String username){
        Optional<RefreshToken> token = this.refreshTokensDatabase.findByUsername(username).map(value->{
                value.setExpireDate(this.calcExpireDate());
                return this.refreshTokensDatabase.save(value);
                });
        return token.isEmpty() ? this.refreshTokensDatabase.save(this.createNewToken(username)) : token.get();
    }

    private RefreshToken createNewToken(String username){
        return new RefreshToken(UUID.randomUUID().toString(),
                username, new Date(), calcExpireDate());
    }

    private Date calcExpireDate(){
        return new Date(System.currentTimeMillis()
                + expirationTime * 1000 * 60 * 60 * 24);
    }

    public Optional<RefreshToken> getAndRenew(String tokenValue){
        return refreshTokensDatabase.findById(tokenValue)
                .map(token ->{
                    if(token.getExpireDate().before(new Date())){
                        refreshTokensDatabase.deleteById(token.getValue());
                        return null;
                    }
                    token.setExpireDate(calcExpireDate());
                    return refreshTokensDatabase.save(token);
                });
    }

    public Optional<RefreshToken> getRefreshToken(String username){
        return refreshTokensDatabase.findByUsername(username);
    }

    public void deleteTokenByUsername(String username){
        this.refreshTokensDatabase.deleteByUsername(username);
    }

    @Scheduled(fixedDelay = 1000 * 60 * 60, initialDelay = 0)
    private void clearExpiredToken(){
        refreshTokensDatabase.deleteByExpireDateLessThan(new Date());
    }


    public Optional<RefreshToken> getRefreshTokenByValue(String value){
        return refreshTokensDatabase.findById(value);
    }
}
