package me.practice.shop.shop.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.practice.shop.shop.models.OrderProductDetail;
import me.practice.shop.shop.models.ShopOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

@Service
public class OrdersService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private JavaMailSender mailSender;

    public boolean sendEmail(ShopOrder order, List<OrderProductDetail> details){
        Context context = new Context();
        OrderParams params = this.getOrderParams(details);
        context.setVariable("order", order);
        context.setVariable("products", details);
        context.setVariable("totalPrice", params.getTotalPrice());
        context.setVariable("totalAmount", params.getTotalAmount());
        String html = this.templateEngine.process("newOrderConfirmation", context);

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
            helper.setSubject("Potwierdzenie zamówienia");
            helper.setFrom("noreplay@bookshop.com");
            helper.setTo(order.getEmail());
            helper.setText(html, true);
            mailSender.send(message);
        }
        catch(MailException | MessagingException e) {
            return false;
        }
        return true;
    }


    public boolean payOrder(String id){
       return this.mongoTemplate.updateFirst(Query.query(Criteria.where("id").is(id)
                       .and("status").is(ShopOrder.UNPAID)),new Update().set("isPaid", true),
               ShopOrder.class).wasAcknowledged();
    }

    public boolean hasUnpaidOrder(String email){
        return this.mongoTemplate.exists(Query.query(Criteria.where("email").is(email)
                .and("status").is(ShopOrder.UNPAID)), ShopOrder.class);
    }


    public OrderParams getOrderParams(List<OrderProductDetail> products){
        double sum = 0;
        int totalAmount = 0;
        for(OrderProductDetail product: products){
            sum += product.getAmount() * product.getProduct().getPrice();
            totalAmount += product.getAmount();
        }
        return new OrderParams(sum, totalAmount);
    }
}

@Data
@AllArgsConstructor
class OrderParams{
    private double totalPrice;
    private int totalAmount;
}
