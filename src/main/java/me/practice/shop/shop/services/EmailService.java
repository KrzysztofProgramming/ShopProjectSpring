package me.practice.shop.shop.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mainSender;

    public void sendTestEmail(){
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("noreplay@bookshop.com");
        mail.setTo("krzychuk112@gmail.com");
        mail.setSubject("Testowy email ze springa");
        mail.setText("Działa zajebiście");
        mainSender.send(mail);
    }

}
