package me.practice.shop.shop.services;

import lombok.Getter;
import me.practice.shop.shop.database.shoppingCarts.ShoppingCartsRepository;
import me.practice.shop.shop.models.ShoppingCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class ShoppingCartsService {

    @Value("${application.cart.expirationTime}")
    private long expirationTime; //in days

    @Getter
    @Value("${application.cart.productsLimit}")
    private long productsLimit;


    @Autowired
    private ShoppingCartsRepository cartsRepository;

    public ShoppingCart getUserShoppingCart(String username){
        return this.cartsRepository.findById(username)
                .orElse(new ShoppingCart(username, new HashMap<>(), calcExpirationTime()));
    }

    public ShoppingCart getAndRenew(String username){
        return renewCart(this.getUserShoppingCart(username));
    }

    public ShoppingCart renewCart(ShoppingCart cart){
        cart.setExpireDate(calcExpirationTime());
        this.cartsRepository.save(cart);
        return cart;
    }

    public void clearUserCart(String username){
        this.cartsRepository.deleteById(username);
    }

    public ShoppingCart cartFromRequest(String username, Map<Long, Integer> products){
        return new ShoppingCart(username, products, calcExpirationTime());
    }

    @Scheduled(fixedDelay = 1000 * 60 * 60, initialDelay = 0)
    private void removeExpiredCarts(){
        cartsRepository.deleteByExpireDateLessThan(new Date());
    }

    private Date calcExpirationTime(){
        return new Date(System.currentTimeMillis() + expirationTime * 1000 * 60 * 60 * 24);
    }

}
