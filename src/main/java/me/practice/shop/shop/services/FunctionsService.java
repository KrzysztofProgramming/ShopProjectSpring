package me.practice.shop.shop.services;

import me.practice.shop.shop.database.users.UsersDatabase;
import me.practice.shop.shop.models.ErrorResponse;
import me.practice.shop.shop.models.ShopUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Function;

@Service
public class FunctionsService {

    @Autowired
    private UsersDatabase usersDatabase;

    public ResponseEntity<?> ifUserLoggedIn(Function<ShopUser, ResponseEntity<?>> fn){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth==null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Użytkownik niezalogowany"));
        }
        Optional<ShopUser> user = this.usersDatabase.findById(auth.getPrincipal().toString());
        if(user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Użytkownik nie istnieje"));
        }
        return fn.apply(user.get());
    }

    public Optional<ShopUser> getUserIfLoggedIn(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null) return Optional.empty();
        return this.usersDatabase.findById(authentication.getPrincipal().toString());
    }
}
