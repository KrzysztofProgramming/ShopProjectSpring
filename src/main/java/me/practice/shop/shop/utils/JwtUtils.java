package me.practice.shop.shop.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import me.practice.shop.shop.models.ShopUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {

    @Value("${application.jwt.key}")
    private String key;

    @Value("${application.jwt.expirationTime}")
    private Integer expirationTime; //in seconds

    public String generateToken(ShopUser userDetails) {
        return Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(key.getBytes()))
                .setSubject(userDetails.getUsername())
                .claim("roles", userDetails.getRoles())
                .claim("authorities", userDetails.getAuthoritiesNumber())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime * 1000))
                .compact();
    }

    public Claims getClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(key.getBytes()))
                .build().parseClaimsJws(token).getBody();
    }

    public String parseUsername(String token){
        return getClaims(token).getSubject();
    }

    public Date parseCreationDate(String token){
        return getClaims(token).getIssuedAt();
    }

    public Date parseExpirationDate(String token){
        return getClaims(token).getExpiration();
    }

    public boolean isTokenExpired(String token){
         return parseExpirationDate(token).before(new Date());
    }

}
