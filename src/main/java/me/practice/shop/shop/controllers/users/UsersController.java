package me.practice.shop.shop.controllers.users;

import me.practice.shop.shop.controllers.users.models.GetUsersParams;
import me.practice.shop.shop.controllers.users.models.UserRequest;
import me.practice.shop.shop.controllers.users.models.UserResponse;
import me.practice.shop.shop.database.users.UsersDatabase;
import me.practice.shop.shop.models.ErrorResponse;
import me.practice.shop.shop.models.ShopUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(value = "api/users")
public class UsersController {

    @Autowired
    private UsersDatabase usersDatabase;

    @Autowired
    private PasswordEncoder encoder;

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
