package me.practice.shop.shop.controllers.users;

import me.practice.shop.shop.controllers.users.models.cart.CartProductRequest;
import me.practice.shop.shop.controllers.users.models.cart.SetCartRequest;
import me.practice.shop.shop.controllers.users.models.profile.EmailRequest;
import me.practice.shop.shop.controllers.users.models.profile.GetOrdersParams;
import me.practice.shop.shop.controllers.users.models.profile.PasswordRequest;
import me.practice.shop.shop.controllers.users.models.profile.ProfileResponse;
import me.practice.shop.shop.controllers.users.models.users.GetUsersParams;
import me.practice.shop.shop.controllers.users.models.users.UserRequest;
import me.practice.shop.shop.controllers.users.models.users.UserResponse;
import me.practice.shop.shop.database.orders.OrdersRepository;
import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.database.shoppingCarts.ShoppingCartsRepository;
import me.practice.shop.shop.database.users.UsersDatabase;
import me.practice.shop.shop.models.*;
import me.practice.shop.shop.permissions.Permissions;
import me.practice.shop.shop.services.FunctionsService;
import me.practice.shop.shop.services.ShoppingCartsService;
import me.practice.shop.shop.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(value = "api/users")
public class UsersController {

    @Autowired
    private UsersDatabase usersDatabase;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private ShoppingCartsRepository cartsRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private ShoppingCartsService cartsService;

    @Autowired
    private UserDetailsServiceImpl userService;

    @Autowired
    private FunctionsService functions;

    @Autowired
    private OrdersRepository ordersRepository;

    @PreAuthorize("hasAuthority('users:read')")
    @GetMapping(value = "byUsername/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id){
        Optional<ShopUser> user = usersDatabase.findById(id);
        return user.isPresent() ? ResponseEntity.ok(user)
                : ResponseEntity.badRequest().body("No user with id: " + id);
    }

    @PreAuthorize("hasAuthority('users:read')")
    @GetMapping(value = "getAll")
    public ResponseEntity<?> getAllUsers(@Valid GetUsersParams params){
        return ResponseEntity.ok(this.usersDatabase.findByGetParams(params)); //todo
    }

    @PreAuthorize("hasAuthority('users:write')")
    @PostMapping(value = "newUser")
    public ResponseEntity<?> addNewUser(@Valid @RequestBody UserRequest request){
        String error = validateUserRequest(request, true);
        if(!error.isEmpty()) return ResponseEntity.badRequest().body(new ErrorResponse(error));
        try {
            return ResponseEntity.ok(toUserResponse(usersDatabase.insert(fromUserRequest(request))));
        }
        catch (IllegalArgumentException e){return ResponseEntity.badRequest()
                .body(new ErrorResponse("Przynajmniej jedna z ról nie istnieje"));}
    }

    @PreAuthorize("hasAuthority('users:write')")
    @PutMapping(value = "updateUser")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserRequest request){
        Optional<ShopUser> user = usersDatabase.findById(request.getUsername());

        if(user.isEmpty()){
            return ResponseEntity.badRequest().body(new ErrorResponse("Brak użytkownika:" + request.getUsername()));
        }

        String error = validateUserRequest(request, false);
        if(!error.isEmpty()) return ResponseEntity.badRequest().body(new ErrorResponse(error));

        try{ return ResponseEntity.ok(toUserResponse(usersDatabase.save(fromUserRequest(request)))); }
        catch (IllegalArgumentException e)
        {return ResponseEntity.badRequest().body(new ErrorResponse("błędne role"));}
    }

    @PreAuthorize("hasAuthority('users:write')")
    @DeleteMapping(value = "deleteUser/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id){
        if(!usersDatabase.existsById(id))
            return ResponseEntity.badRequest().body(new ErrorResponse("No user with id: " + id));
        usersDatabase.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("profile/info")
    public ResponseEntity<?> getProfile(){
        return functions.ifUserLoggedIn(user -> ResponseEntity
                .ok(new ProfileResponse(user.getUsername(), user.getEmail(), user.getUserInfo())));
    }

    @PutMapping("profile/updateEmail")
    public ResponseEntity<?> modifyProfile(@Valid @RequestBody EmailRequest emailRequest){
        return functions.ifUserLoggedIn(user->{
            if(!encoder.matches(emailRequest.getPassword(), user.getPassword()))
                return ResponseEntity.badRequest().body(new ErrorResponse("Błędne hasło"));
            if(this.usersDatabase.existsByEmail(emailRequest.getNewEmail())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Ten email jest już zajęty"));
            }
            user.setEmail(emailRequest.getNewEmail());
            user = this.usersDatabase.save(user);
            return ResponseEntity.ok(new ProfileResponse(user.getUsername(), user.getEmail(), user.getUserInfo()));
        });
    }

    @PutMapping("profile/updatePassword")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody PasswordRequest passwordRequest){
        return functions.ifUserLoggedIn(user->{
            if(!encoder.matches(passwordRequest.getOldPassword(), user.getPassword()))
                return ResponseEntity.badRequest().build();
            user.setPassword(encoder.encode(passwordRequest.getNewPassword()));
            this.usersDatabase.save(user);
            return ResponseEntity.ok().build();
        });
    }

    @PutMapping("profile/updateUserInfo")
    public ResponseEntity<?> updateUserInfo(@Valid @RequestBody UserInfo info){
        return functions.ifUserLoggedIn(user->{
            if(this.usersDatabase.saveUserInfo(user.getUsername(), info).wasAcknowledged())
                return ResponseEntity.ok().build();
            return ResponseEntity.badRequest().body(new ErrorResponse("Operacja nie udana"));
        });
    }

    @GetMapping("profile/orders")
    public ResponseEntity<?> getUserOrders(@Valid GetOrdersParams params){
        return this.functions.ifUserLoggedIn(user ->{
//            return ResponseEntity.ok(new GetByParamsResponse<>())
            Page<ShopOrder> page = this.ordersRepository.getByParams(params, user.getUsername());
            return ResponseEntity.ok(new GetByParamsResponse<>(
                    page.getNumber(), page.getTotalPages(), page.getTotalElements(), page.getContent()));
        });

    }
//    @GetMapping("cart/test")
//    public ResponseEntity<?> addProductToCart(){
//        return ResponseEntity.ok(this.cartsRepository.save(new ShoppingCart("siema",
//                Map.of("produkt1", 10, "produkt2", 15),
//                new Date(System.currentTimeMillis() + 1000 * 60 * 20))));
////        return ResponseEntity.ok(this.cartsRepository.findById("33c2ddd8-9149-4e68-a3b1-6570ceba282f"));
//
//    }

    @PutMapping("cart/setProduct")
    public ResponseEntity<?> setProductInCart(@Valid @RequestBody CartProductRequest request){
        return functions.ifUserLoggedIn(user->{
            ShoppingCart cart = cartsService.getAndRenew(user.getUsername());
            return this.modifyCartProduct(request, cart);
        });
    }

    private ResponseEntity<?> modifyCartProduct(CartProductRequest request, ShoppingCart cart){
        if(cart.getItems().size() >= this.cartsService.getProductsLimit())
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    "Maksymalna ilość różnych produków w karcie wynosi: "+ this.cartsService.getProductsLimit()));
        Optional<BookProduct> product = this.productsRepository.findById(request.getProductId());
        if(product.isEmpty())
            return ResponseEntity.badRequest().body(new ErrorResponse("Taki produkt nie istnieje"));
        if(product.get().getInStock() < request.getAmount())
            return ResponseEntity.badRequest().body(new ErrorResponse("Podana ilość nie jest już dostępna"));
        cart.getItems().put(product.get().getId(), request.getAmount());
        return ResponseEntity.ok(this.cartsRepository.save(cart));
    }

    @PutMapping("cart/addProduct")
    public ResponseEntity<?> addProductToCart(@Valid @RequestBody CartProductRequest request){
        return functions.ifUserLoggedIn(user->{
            ShoppingCart cart = cartsService.getAndRenew(user.getUsername());
            return this.modifyCartProduct(new CartProductRequest(request.getProductId(), cart.getItems()
                    .getOrDefault(request.getProductId(),0) + request.getAmount()), cart);
        });
    }

    @PutMapping("cart/setCart")
    public ResponseEntity<?> setCart(@Valid @RequestBody SetCartRequest request){
        return functions.ifUserLoggedIn(user->{
            ShoppingCart cart = this.cartsService.cartFromRequest(user.getUsername(), request.getProducts());
            List<String> productIds = cart.getItems().keySet().stream().toList();
            Iterable<BookProduct> products = this.productsRepository.findAllById(productIds);

            if(productIds.size() != StreamSupport.stream(products.spliterator(), false).count())
                return ResponseEntity.badRequest().body(new ErrorResponse("Podano błędny produkt"));

            if(!StreamSupport.stream(products.spliterator(), false).allMatch(product->
                    !cart.getItems().containsKey(product.getId()) ||
                    product.getInStock()
                    >= cart.getItems().get(product.getId()))
            ) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Nie wszystkie produkty są dostępne"));
            }
            return ResponseEntity.ok(this.cartsRepository.save(cart));
        });
    }

    @DeleteMapping("cart/deleteProduct/{id}")
    public ResponseEntity<?> deleteCartProduct(@PathVariable String id){
        return functions.ifUserLoggedIn(user->{
            Optional<ShoppingCart> cart = this.cartsRepository.findById(user.getUsername());
            if(cart.isEmpty()) return ResponseEntity.ok().build();
            cart.get().getItems().remove(id);
            if(cart.get().getItems().size() == 0)
                this.cartsRepository.deleteById(cart.get().getOwnerUsername());
            else
                this.cartsRepository.save(cart.get());
            return ResponseEntity.ok().body(cart.get());
        });
    }

    @DeleteMapping("cart/deleteCart")
    public ResponseEntity<?> deleteCart(){
        return functions.ifUserLoggedIn(user->{
            this.cartsRepository.deleteById(user.getUsername());
            return ResponseEntity.ok().build();
        });
    }

    @GetMapping("cart/getCart")
    public ResponseEntity<?> getCart(){
        return functions.ifUserLoggedIn(user-> ResponseEntity.ok(this.cartsService.getAndRenew(user.getUsername())));
    }

    private UserResponse toUserResponse(ShopUser user){
        return new UserResponse(user.getUsername(), user.getEmail(), Permissions.toNumber(user.getAuthorities()), user.getRolesStrings());
    }

    private String validateUserRequest(UserRequest request, boolean validateUsername){
//        if(this.usersDatabase.findByEmailOrUsername(request.getUsername(), request.getEmail()).isPresent())
        if(usernameExists(request.getUsername()) && validateUsername)
            return "Taka nazwa użytkownika już istnieje";
        if(emailExist(request.getEmail()))
            return "Ten email jest już zajęty";
        return "";
    }

    private boolean usernameExists(String username){
        return usersDatabase.existsById(username);
    }

    private boolean emailExist(String email){
        return usersDatabase.existsByEmail(email);
    }

    private ShopUser fromUserRequest(UserRequest r) throws IllegalArgumentException{
        return new ShopUser(r.getUsername(), r.getEmail(),
                encoder.encode(r.getPassword()),this.userService.getRolesByNames(r.getRoles()), r.getInfo());
    }

}
