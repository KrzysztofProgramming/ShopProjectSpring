package me.practice.shop.shop.controllers.users;

import me.practice.shop.shop.controllers.users.models.PasswordRequest;
import me.practice.shop.shop.controllers.users.models.cart.CartProductRequest;
import me.practice.shop.shop.controllers.users.models.cart.SetCartRequest;
import me.practice.shop.shop.controllers.users.models.profile.ProfileRequest;
import me.practice.shop.shop.controllers.users.models.profile.ProfileResponse;
import me.practice.shop.shop.controllers.users.models.users.GetUsersParams;
import me.practice.shop.shop.controllers.users.models.users.UserRequest;
import me.practice.shop.shop.controllers.users.models.users.UserResponse;
import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.database.shoppingCarts.ShoppingCartsRepository;
import me.practice.shop.shop.database.users.UsersDatabase;
import me.practice.shop.shop.models.BookProduct;
import me.practice.shop.shop.models.ErrorResponse;
import me.practice.shop.shop.models.ShopUser;
import me.practice.shop.shop.models.ShoppingCart;
import me.practice.shop.shop.services.ShoppingCartsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
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

    @PreAuthorize("hasAuthority('USERS_GET')")
    @GetMapping(value = "byUsername/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id){
        Optional<ShopUser> user = usersDatabase.findById(id);
        return user.isPresent() ? ResponseEntity.ok(user)
                : ResponseEntity.badRequest().body("No user with id: " + id);
    }

    @PreAuthorize("hasAuthority('USERS_GET')")
    @GetMapping(value = "getAll")
    public ResponseEntity<?> getAllUsers(@Valid GetUsersParams params){
        Pageable pageable = PageRequest.of(params.getPageNumber(), params.getPageSize());
        return ResponseEntity.ok("siema"); //todo
    }

    @PreAuthorize("hasAuthority('USERS_MODIFY')")
    @PostMapping(value = "addNewUser")
    public ResponseEntity<?> addNewUser(@Valid @RequestBody UserRequest request){
        String error = validateUserRequest(request);
        if(!error.isEmpty()) return ResponseEntity.badRequest().body(new ErrorResponse(error));
        return ResponseEntity.ok(toUserResponse(usersDatabase.insert(fromUserRequest(request))));
    }

    @PreAuthorize("hasAuthority('USERS_MODIFY')")
    @PutMapping(value = "updateUserByUsername/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @Valid @RequestBody UserRequest request){
        Optional<ShopUser> user = usersDatabase.findById(id);

        if(user.isEmpty()){
            return ResponseEntity.badRequest().body(new ErrorResponse("No user with id:" + id));
        }

        if(!user.get().getEmail().equals(request.getEmail()) && emailExist(request.getEmail()))
            return ResponseEntity.badRequest().body(new ErrorResponse("Email already taken"));

        if(!id.equals(request.getUsername()) && usernameExists(request.getUsername()))
            return ResponseEntity.badRequest().body(new ErrorResponse("Username already taken"));

        if(!id.equals(request.getUsername()))
            usersDatabase.deleteById(id);

        return ResponseEntity.ok(toUserResponse(usersDatabase.save(fromUserRequest(request))));
    }

    @PreAuthorize("hasAuthority('USERS_MODIFY')")
    @DeleteMapping(value = "deleteUser/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id){
        if(!usersDatabase.existsById(id))
            return ResponseEntity.badRequest().body(new ErrorResponse("No user with id: " + id));
        usersDatabase.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("profile/info")
    public ResponseEntity<?> getProfile(){
        return this.ifUserLoggedIn(user -> ResponseEntity
                .ok(new ProfileResponse(user.getUsername(), user.getEmail())));
    }

    @PutMapping("profile/update")
    public ResponseEntity<?> modifyProfile(@Valid @RequestBody ProfileRequest profileRequest){
        return this.ifUserLoggedIn(user->{
            System.out.println(profileRequest);
            if(this.usersDatabase.existsByEmail(profileRequest.getEmail())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Ten email jest już zajęty"));
            }
            user.setEmail(profileRequest.getEmail());
            this.usersDatabase.save(user);
            return ResponseEntity.ok(new ProfileResponse(user.getUsername(), user.getEmail()));
        });
    }

    @PutMapping("profile/updatePassword")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody PasswordRequest passwordRequest){
        return this.ifUserLoggedIn(user->{
            if(!encoder.matches(passwordRequest.getOldPassword(), user.getPassword()))
                return ResponseEntity.badRequest().build();
            user.setPassword(encoder.encode(passwordRequest.getNewPassword()));
            this.usersDatabase.save(user);
            return ResponseEntity.ok().build();
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
        return this.ifUserLoggedIn(user->{
            ShoppingCart cart = cartsService.getAndRenew(user.getUsername());
            return this.modifyCartProduct(request, cart);
        });
    }

    private ResponseEntity<?> modifyCartProduct(CartProductRequest request, ShoppingCart cart){
        int previousAmount = cart.getItems().getOrDefault(request.getProductId(), 0);

        Optional<BookProduct> product = this.productsRepository.findById(request.getProductId());
        if(product.isEmpty())
            return ResponseEntity.badRequest().body(new ErrorResponse("Taki produkt nie istnieje"));
        if(product.get().getInStock() + previousAmount < request.getAmount())
            return ResponseEntity.badRequest().body(new ErrorResponse("Podana ilość nie jest już dostępna"));

        product.get().setInStock(product.get().getInStock() - request.getAmount() + previousAmount);
        cart.getItems().put(request.getProductId(), request.getAmount());
        this.productsRepository.save(product.get());
        return ResponseEntity.ok(this.cartsRepository.save(cart));
    }

    @PutMapping("cart/addProduct")
    public ResponseEntity<?> addProductToCart(@Valid @RequestBody CartProductRequest request){
        return this.ifUserLoggedIn(user->{
            ShoppingCart cart = cartsService.getAndRenew(user.getUsername());
            return this.modifyCartProduct(new CartProductRequest(request.getProductId(), cart.getItems()
                    .getOrDefault(request.getProductId(),0) + request.getAmount()), cart);
        });
    }

    @PutMapping("cart/setCart")
    public ResponseEntity<?> setCart(@Valid @RequestBody SetCartRequest request){
        return this.ifUserLoggedIn(user->{
            ShoppingCart cart = this.cartsService.cartFromRequest(user.getUsername(), request.getProducts());
            List<String> productIds = cart.getItems().keySet().stream().toList();
            Iterable<BookProduct> products = this.productsRepository.findAllById(productIds);

            if(productIds.size() != StreamSupport.stream(products.spliterator(), false).count())
                return ResponseEntity.badRequest().body(new ErrorResponse("Podano błędny produkt"));

            ShoppingCart oldCart = this.cartsService.getUserShoppingCart(user.getUsername());

            if(!StreamSupport.stream(products.spliterator(), false).allMatch(product->
                    !cart.getItems().containsKey(product.getId()) ||
                    product.getInStock() + oldCart.getItems().getOrDefault(product.getId(), 0)
                    >= cart.getItems().get(product.getId()))
            ) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Nie wszystkie produkty są dostępne"));
            }

            List<BookProduct> updateProducts = StreamSupport.stream(products.spliterator(), false)
                    .peek(product-> product.setInStock(product.getInStock() - cart.getItems().get(product.getId())
                            + oldCart.getItems().getOrDefault(product.getId(), 0)))
                    .collect(Collectors.toList());

            this.productsRepository.saveAll(updateProducts);
            return ResponseEntity.ok(this.cartsRepository.save(cart));
        });
    }

    @GetMapping("cart/getCart")
    public ResponseEntity<?> getCart(){
        return this.ifUserLoggedIn(user-> ResponseEntity.ok(cartsService.getUserShoppingCart(user.getUsername())));
    }

    private ResponseEntity<?> ifUserLoggedIn(Function<ShopUser, ResponseEntity<?>> fn){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth==null){
            return ResponseEntity.badRequest().body(new ErrorResponse("Użytkownik niezalogowany"));
        }
        Optional<ShopUser> user = this.usersDatabase.findById((String) auth.getPrincipal());
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Użytkownik nie istnieje"));
        }
        return fn.apply(user.get());
    }

    private UserResponse toUserResponse(ShopUser user){
        return new UserResponse(user.getUsername(), user.getEmail(), user.getAuthorities(), user.getRoles());
    }

    private String validateUserRequest(UserRequest request){
        if(usernameExists(request.getUsername()))
            return "Username already taken";
        if(emailExist(request.getEmail()))
            return "Email already taken";
        return "";
    }

    private boolean usernameExists(String username){
        return usersDatabase.existsById(username);
    }

    private boolean emailExist(String email){
        return usersDatabase.existsByEmail(email);
    }

    private ShopUser fromUserRequest(UserRequest r){
        return new ShopUser(r.getUsername(), r.getEmail(),
                encoder.encode(r.getPassword()),r.getRoles(), r.getAuthorities());
    }



}
