package me.practice.shop.shop.services;

import me.practice.shop.shop.database.resetTokens.ResetTokensRepository;
import me.practice.shop.shop.models.ResetPasswordToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class ResetTokensService {

    @Value("${application.passwordReset.expirationTime}")
    private long tokenExpirationTime; //minutes

    @Autowired
    private ResetTokensRepository tokensRepository;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private JavaMailSender mailSender;

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

    public Optional<ResetPasswordToken> getTokenByValue(String token){
        return this.tokensRepository.findById(token);
    }

    public void deleteToken(String token){
        this.tokensRepository.deleteById(token);
    }

    private Date calcExpirationTime(){
        return new Date(System.currentTimeMillis() + tokenExpirationTime * 60 * 1000);
    }

    public void generateTokenAndSendEmail(String username, String email) throws MessagingException {
        ResetPasswordToken token = this.generateNewToken(username);
        Context context = new Context();
        context.setVariable("link", "http://localhost:4200/resetPassword/" + token.getToken());
        context.setVariable("username", username);
        context.setVariable("image", "image");
        String html = this.templateEngine.process("passwordResetTemplate", context);
        this.sendEmail(email, html);
    }

    private void sendEmail(String userEmail, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,true, "utf-8");
        helper.setSubject("Reset hasła w BookShop");
        helper.setFrom("noreplay@bookshop.com");
        helper.setTo(userEmail);
        helper.setText(content, true);
        mailSender.send(message);
    }

    @Scheduled(fixedDelay = 1000 * 60 * 60, initialDelay = 0)
    private void clearExpiredToken(){
        this.tokensRepository.deleteByExpireDateLessThan(new Date());
    }
}