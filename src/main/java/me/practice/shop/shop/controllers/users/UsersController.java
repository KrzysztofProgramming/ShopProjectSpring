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
import me.practice.shop.shop.database.orders.OrdersSearcher;
import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.database.shoppingCarts.ShoppingCartsRepository;
import me.practice.shop.shop.database.users.UsersRepository;
import me.practice.shop.shop.models.*;
import me.practice.shop.shop.permissions.Permissions;
import me.practice.shop.shop.services.FunctionsService;
import me.practice.shop.shop.services.ShoppingCartsService;
import me.practice.shop.shop.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "api/users")
public class UsersController {

    @Autowired
    private UsersRepository usersRepository;

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

    @Autowired
    private OrdersSearcher ordersSearcher;

    @PreAuthorize("hasAuthority('users:read')")
    @GetMapping(value = "byUsername/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id){
        Optional<ShopUser> user = usersRepository.findById(id);
        return user.isPresent() ? ResponseEntity.ok(user)
                : ResponseEntity.badRequest().body("No user with id: " + id);
    }

    @PreAuthorize("hasAuthority('users:read')")
    @GetMapping(value = "getAll")
    public ResponseEntity<?> getAllUsers(@Valid GetUsersParams params){
        Page<ShopUser> users = this.usersRepository.findAll(PageRequest.of(params.getPageSize() - 1,
                params.getPageNumber()).withSort(Sort.by("id")));
        return ResponseEntity.ok(new GetByParamsResponse<>(users.getNumber(), users.getTotalPages(),
                users.getTotalElements(), users.toList())); //todo
    }

    @PreAuthorize("hasAuthority('users:write')")
    @PostMapping(value = "newUser")
    public ResponseEntity<?> addNewUser(@Valid @RequestBody UserRequest request){
        String error = validateUserRequest(request, true);
        if(!error.isEmpty()) return ResponseEntity.badRequest().body(new ErrorResponse(error));
        try {
            return ResponseEntity.ok(toUserResponse(usersRepository.save(fromUserRequest(request))));
        }
        catch (IllegalArgumentException e){return ResponseEntity.badRequest()
                .body(new ErrorResponse("Przynajmniej jedna z r??l nie istnieje"));}
    }

    @PreAuthorize("hasAuthority('users:write')")
    @PutMapping(value = "updateUser")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserRequest request){
        Optional<ShopUser> user = usersRepository.findById(request.getUsername());

        if(user.isEmpty()){
            return ResponseEntity.badRequest().body(new ErrorResponse("Brak u??ytkownika:" + request.getUsername()));
        }

        String error = validateUserRequest(request, false);
        if(!error.isEmpty()) return ResponseEntity.badRequest().body(new ErrorResponse(error));

        try{ return ResponseEntity.ok(toUserResponse(usersRepository.save(fromUserRequest(request)))); }
        catch (IllegalArgumentException e)
        {return ResponseEntity.badRequest().body(new ErrorResponse("b????dne role"));}
    }

    @PreAuthorize("hasAuthority('users:write')")
    @DeleteMapping(value = "deleteUser/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id){
        if(!usersRepository.existsById(id))
            return ResponseEntity.badRequest().body(new ErrorResponse("No user with id: " + id));
        usersRepository.deleteById(id);
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
                return ResponseEntity.badRequest().body(new ErrorResponse("B????dne has??o"));
            if(this.usersRepository.existsByEmail(emailRequest.getNewEmail())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Ten email jest ju?? zaj??ty"));
            }
            user.setEmail(emailRequest.getNewEmail());
            user = this.usersRepository.save(user);
            return ResponseEntity.ok(new ProfileResponse(user.getUsername(), user.getEmail(), user.getUserInfo()));
        });
    }

    @PutMapping("profile/updatePassword")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody PasswordRequest passwordRequest){
        return functions.ifUserLoggedIn(user->{
            if(!encoder.matches(passwordRequest.getOldPassword(), user.getPassword()))
                return ResponseEntity.badRequest().build();
            user.setPassword(encoder.encode(passwordRequest.getNewPassword()));
            this.usersRepository.save(user);
            return ResponseEntity.ok().build();
        });
    }

    @PutMapping("profile/updateUserInfo")
    public ResponseEntity<?> updateUserInfo(@Valid @RequestBody UserInfo info){
        return functions.ifUserLoggedIn(user->{
            user.setUserInfo(info);
            this.usersRepository.save(user);
            return ResponseEntity.ok().build();
        });
    }

    @GetMapping("profile/order/{id}")
    public ResponseEntity<?> getUserOrder(@PathVariable Long id){
        return this.functions.ifUserLoggedIn(user->{
            Optional<ShopOrder> order = this.ordersRepository.findUserOrderById(user.getUsername(), id);
            if(order.isPresent()) return ResponseEntity.ok(order);
            return ResponseEntity.badRequest().body(new ErrorResponse("U??ytkownik nie posiada takiego zam??wienia"));
        });
    }

    @GetMapping("profile/orders")
    public ResponseEntity<?> getUserOrders(@Valid GetOrdersParams params){
        return this.functions.ifUserLoggedIn(user ->{
            Page<ShopOrder> page = this.ordersSearcher.getByParams(params, user.getUsername());
            return ResponseEntity.ok(new GetByParamsResponse<>(
                    page.getNumber() + 1, page.getTotalPages(), page.getTotalElements(), page.getContent()));
        });

    }

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
                    "Maksymalna ilo???? r????nych produk??w w karcie wynosi: "+ this.cartsService.getProductsLimit()));
        Optional<BookProduct> product = this.productsRepository.findById(request.getProductId());
        if(product.isEmpty())
            return ResponseEntity.badRequest().body(new ErrorResponse("Taki produkt nie istnieje"));
        if(product.get().getInStock() < request.getAmount())
            return ResponseEntity.badRequest().body(new ErrorResponse("Podana ilo???? nie jest ju?? dost??pna"));
        if(product.get().getIsArchived())
            return ResponseEntity.badRequest().body(new ErrorResponse("Ten produkt zosta?? wycofany"));
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
            List<Long> productsIds = cart.getItems().keySet().stream().toList();
            List<BookProduct> products = this.productsRepository.findAllById(productsIds);
            if(products.size() < productsIds.size())
                return ResponseEntity.badRequest().body(new ErrorResponse("Niekt??re z podanych produkt??w nie istniej??"));

            if(productsIds.size() != (long) products.size())
                return ResponseEntity.badRequest().body(new ErrorResponse("Podano b????dny produkt"));

            if(products.stream().anyMatch(product->
                    !cart.getItems().containsKey(product.getId()) ||
                    product.getInStock()
                    <= cart.getItems().get(product.getId()) || !product.getIsArchived())
            ) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Nie wszystkie produkty s?? dost??pne"));
            }
            return ResponseEntity.ok(this.cartsRepository.save(cart));
        });
    }

    @DeleteMapping("cart/deleteProduct/{id}")
    public ResponseEntity<?> deleteCartProduct(@PathVariable Long id){
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
            try {
                this.cartsRepository.deleteById(user.getUsername());
            }
            catch (EmptyResultDataAccessException ignore){}
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
            return "Taka nazwa u??ytkownika ju?? istnieje";
        if(emailExist(request.getEmail()))
            return "Ten email jest ju?? zaj??ty";
        return "";
    }

    private boolean usernameExists(String username){
        return usersRepository.existsById(username);
    }

    private boolean emailExist(String email){
        return usersRepository.existsByEmail(email);
    }

    private ShopUser fromUserRequest(UserRequest r) throws IllegalArgumentException{
        return new ShopUser(r.getUsername(), r.getEmail(),
                encoder.encode(r.getPassword()),this.userService.getRolesByNames(r.getRoles()), r.getInfo());
    }

}
