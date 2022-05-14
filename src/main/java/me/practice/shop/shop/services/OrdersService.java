package me.practice.shop.shop.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.practice.shop.shop.database.orders.OrdersRepository;
import me.practice.shop.shop.models.OrderProductDetail;
import me.practice.shop.shop.models.ShopOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

@Service
public class OrdersService {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private OrdersRepository ordersRepository;

    public boolean sendEmail(ShopOrder order, List<OrderProductDetail> details){
        Context context = new Context();
        final String baseUrl =
                ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        OrderParams params = this.getOrderParams(details);
        context.setVariable("link", baseUrl + "/payOrder/" + order.getId());
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


    public boolean payOrder(Long id){
       return this.ordersRepository.changeStatus(id, ShopOrder.PAID, ShopOrder.UNPAID) > 0;
    }

    public boolean hasUnpaidOrder(String email){
        return this.ordersRepository.countOrders(email, ShopOrder.UNPAID) > 0;
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
