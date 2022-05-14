package me.practice.shop.shop.controllers.orders;

import me.practice.shop.shop.controllers.orders.models.NewOrderRequest;
import me.practice.shop.shop.database.orders.OrdersRepository;
import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.models.*;
import me.practice.shop.shop.services.FunctionsService;
import me.practice.shop.shop.services.OrdersService;
import me.practice.shop.shop.services.ShoppingCartsService;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "api/orders/")
public class OrdersController {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private FunctionsService functionsService;

    @Autowired
    private ShoppingCartsService cartsService;

    @Autowired
    private EntityManager entityManager;

    @PostMapping("newOrder")
    public ResponseEntity<?> createNewOrder(@Valid @RequestBody NewOrderRequest request){
        if(this.ordersService.hasUnpaidOrder(request.getEmail()))
            return ResponseEntity.badRequest().body(new ErrorResponse("Dokończ płatność poprzedniego zamówienia"));
        List<BookProduct> products = this.productsRepository.findAllById(request.getProducts().keySet());
        if(products.size() < request.getProducts().size()){
            return ResponseEntity.badRequest().body("Nie wszystkie produkty istnieją");
        }
        if(!areProductsAvailable(request, products)){
            return ResponseEntity.badRequest().body("Nie wszystkie produkty są dostępne");
        }
        Optional<ShopUser> user = this.functionsService.getUserIfLoggedIn();
        ShopOrder order = fromRequest(request, products, user.isEmpty() ? null : user.get().getUsername());
        order = this.ordersRepository.save(order);

        if(!this.ordersService.sendEmail(order, products.stream().map(product->new OrderProductDetail(product,
                request.getProducts().get(product.getId()))).collect(Collectors.toList()))){
            this.ordersRepository.deleteById(order.getId());
            return ResponseEntity.badRequest().body(new ErrorResponse("Nie udało się wysłać emaila"));
        }
        this.decrementProductsInStock(request);
//        user.ifPresent(shopUser -> this.cartsService.clearUserCart(shopUser.getUsername()));
        return ResponseEntity.ok().body(order);
    }

    @PutMapping("payOrder/{id}")
    public ResponseEntity<?> payOrder(@PathVariable Long id){
        return this.ordersService.payOrder(id) ? ResponseEntity.ok().build() :
                ResponseEntity.badRequest().body(new ErrorResponse("Błąd przy płatności zamówienia"));
    }

    private boolean areProductsAvailable(NewOrderRequest request, List<BookProduct> products){
        try {
            return products.stream().allMatch(product -> !product.getIsArchived()
                    && request.getProducts().get(product.getId()) <= product.getInStock());
        }
        catch (NullPointerException e){
            return false;
        }
    }

    private double calcTotalPrice(List<BookProduct> products, Map<Long, Integer> amount){
        double price = 0;
        for(BookProduct product: products){
            price += amount.get(product.getId()) * product.getPrice();
        }
        return price;
    }

    private ShopOrder fromRequest(NewOrderRequest request, List<BookProduct> products, String username){
        return new ShopOrder(username, request.getEmail(), request.getInfo(),
                request.getProducts(), new Date(), this.calcTotalPrice(products, request.getProducts()),
                ShopOrder.UNPAID);
    }

    private void decrementProductsInStock(NewOrderRequest request){
        Session session = this.entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            PreparedStatement statement = connection.prepareStatement("UPDATE book_products_table" +
                    " SET in_stock = in_stock - ? WHERE book_id = ?");
            request.getProducts().forEach((key, value)->{
                try {
                    statement.setInt(1, value);
                    statement.setLong(2, key);
                    statement.addBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            statement.executeBatch();
        });
    }
}
