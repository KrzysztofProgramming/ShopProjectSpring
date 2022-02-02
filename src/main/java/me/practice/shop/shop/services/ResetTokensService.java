package me.practice.shop.shop.services;

import me.practice.shop.shop.database.resetTokens.ResetTokensRepository;
import me.practice.shop.shop.models.ResetPasswordToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.TemplateEngine;

import java.util.Date;
import java.util.UUID;

@Controller
@Service
public class ResetTokensService {

    @Value("${application.passwordReset.expirationTime}")
    private long tokenExpirationTime; //minutes

    @Autowired
    private ResetTokensRepository tokensRepository;

    @Autowired
    private TemplateEngine templateEngine;

    public ResetPasswordToken generateNewToken(String username){
        this.tokensRepository.deleteByUsername(username);
        return this.tokensRepository.insert(new ResetPasswordToken(UUID.randomUUID().toString(), username,
                new Date(), this.calcExpirationTime()));
    }

    public boolean tokenExist(String token){
        return this.tokensRepository.findById(token).isPresent();
    }

    public void deleteTokenByUsername(String username){
        this.tokensRepository.deleteByUsername(username);
    }

    public void deleteToken(String token){
        this.tokensRepository.deleteById(token);
    }

    private Date calcExpirationTime(){
        return new Date(System.currentTimeMillis() + tokenExpirationTime * 60 * 1000);
    }

    @GetMapping(value = "siema")
    public String test(){
        return "passwordResetTemplate.html";
    }
}
