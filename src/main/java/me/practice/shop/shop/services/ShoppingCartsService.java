package me.practice.shop.shop.services;

import me.practice.shop.shop.database.products.ProductsDatabase;
import me.practice.shop.shop.database.shoppingCarts.ShoppingCartsRepository;
import me.practice.shop.shop.models.ShopProduct;
import me.practice.shop.shop.models.ShoppingCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ShoppingCartsService {

    @Value("${application.cart.expirationTime}")
    private int expirationTime; //in minutes

    @Autowired
    private ShoppingCartsRepository cartsRepository;

    @Autowired
    private ProductsDatabase productsDatabase;

    public ShoppingCart getUserShoppingCart(String username){
        return this.cartsRepository.findById(username)
                .orElse(new ShoppingCart(username, new HashMap<>(),
                        new Date(System.currentTimeMillis() + expirationTime * 1000 * 60)));
    }

    public ShoppingCart cartFromRequest(String username, Map<String, Integer> products){
        return new ShoppingCart(username, products, new Date(System.currentTimeMillis() + expirationTime * 1000 * 60));
    }


    @Scheduled(fixedDelay = 1000 * 60, initialDelay = 0)
    private void removeExpiredCarts(){
        List<ShoppingCart> carts = cartsRepository.deleteByExpireDateLessThan(new Date());
        List<String> productsToUpdate = carts.stream()
                .flatMap(item->item.getItems().keySet().stream()).collect(Collectors.toList());
        List<ShopProduct> updatedProducts = StreamSupport.stream(this.productsDatabase.findAllById(productsToUpdate).spliterator(), false)
                .peek(product->product.setInStock(product.getInStock() + sumProductInStock(carts, product.getId())))
                .collect(Collectors.toList());
        this.productsDatabase.saveAll(updatedProducts);
    }

    private int sumProductInStock(List<ShoppingCart> carts, String productId){
        int sum = 0;
        for(ShoppingCart cart: carts){
            sum += cart.getItems().getOrDefault(productId, 0);
        }
        return sum;
    }
}
