package me.practice.shop.shop.controllers.orders;

import me.practice.shop.shop.controllers.orders.models.NewOrderRequest;
import me.practice.shop.shop.controllers.orders.models.PayOrderRequest;
import me.practice.shop.shop.database.orders.OrdersRepository;
import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.database.users.UsersDatabase;
import me.practice.shop.shop.models.*;
import me.practice.shop.shop.services.FunctionsService;
import me.practice.shop.shop.services.OrdersService;
import me.practice.shop.shop.utils.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(value = "api/orders/")
public class OrdersController {

    @Autowired
    private UsersDatabase usersDatabase;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private FunctionsService functionsService;

    @PostMapping("newOrder")
    public ResponseEntity<?> createNewOrder(@Valid @RequestBody NewOrderRequest request){
        if(this.ordersService.hasUnpaidOrder(request.getEmail()))
            return ResponseEntity.badRequest().body(new ErrorResponse("Dokończ płatność poprzedniego zamówienia"));
        List<BookProduct> products = IterableUtils.toList(this.productsRepository
                .findAllById(request.getProducts().keySet()));
        if(products.size() < request.getProducts().size()){
            return ResponseEntity.badRequest().body("Nie wszystkie produkty są dostępne");
        }
        Optional<ShopUser> user = this.functionsService.getUserIfLoggedIn();
        ShopOrder order = fromRequest(request, products, user.isEmpty() ? null : user.get().getUsername());

        if(!this.ordersService.sendEmail(order, products.stream().map(product->new OrderProductDetail(product,
                request.getProducts().get(product.getId()))).collect(Collectors.toList()))){
            return ResponseEntity.badRequest().body(new ErrorResponse("Nie udało się wysłać emaila"));
        }
//        user.ifPresent(shopUser -> this.cartsService.clearUserCart(shopUser.getUsername()));
        this.productsRepository.decreaseProductsCounts(request.getProducts());
        this.ordersRepository.insert(order);
        return ResponseEntity.ok().body(order);
    }

    @PutMapping("payOrder")
    public ResponseEntity<?> payOrder(@Valid @RequestBody PayOrderRequest request){
        return this.ordersService.payOrder(request.getId()) ? ResponseEntity.ok().build() :
                ResponseEntity.badRequest().body(new ErrorResponse("Brak zamówienia o podanym id"));
    }

    private double calcTotalPrice(List<BookProduct> products, Map<String, Integer> amount){
        double price = 0;
        for(BookProduct product: products){
            price += amount.get(product.getId()) * product.getPrice();
        }
        return price;
    }

    private ShopOrder fromRequest(NewOrderRequest request, List<BookProduct> products, String username){
        return new ShopOrder(UUID.randomUUID().toString(), username, request.getEmail(), request.getInfo(),
                request.getProducts(), new Date(), this.calcTotalPrice(products, request.getProducts()),
                ShopOrder.UNPAID);
    }
}
