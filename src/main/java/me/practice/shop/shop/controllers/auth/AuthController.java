package me.practice.shop.shop.controllers.auth;

import me.practice.shop.shop.controllers.auth.models.LoginRequest;
import me.practice.shop.shop.controllers.auth.models.LoginResponse;
import me.practice.shop.shop.controllers.auth.models.RefreshRequest;
import me.practice.shop.shop.controllers.auth.models.RegisterRequest;
import me.practice.shop.shop.database.users.UsersDatabase;
import me.practice.shop.shop.models.ErrorResponse;
import me.practice.shop.shop.models.RefreshToken;
import me.practice.shop.shop.models.ShopUser;
import me.practice.shop.shop.services.RefreshTokensService;
import me.practice.shop.shop.services.UserDetailsServiceImpl;
import me.practice.shop.shop.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(value = "api/auth/")
public class AuthController {

    @Autowired
    private UsersDatabase usersDatabase;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokensService refreshTokensService;

    @PostMapping(value = "register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request){
        if(usersDatabase.existsById(request.getUsername()))
            return ResponseEntity.badRequest().body(new ErrorResponse("Username already taken"));
        if(usersDatabase.existsByEmail(request.getEmail()))
            return ResponseEntity.badRequest().body(new ErrorResponse("Email already taken"));

        usersDatabase.insert(new ShopUser(request.getUsername(), request.getEmail(),
                encoder.encode(request.getPassword()), Collections.emptyList()));

        return loginWithUsername(new LoginRequest(request.getUsername(), request.getPassword()));
    }

    @PostMapping(value = "login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request){
        String username = usersDatabase.findByEmail(request.getUsernameOrEmail())
                .map(ShopUser::getUsername).orElse(request.getUsernameOrEmail());

        request.setUsernameOrEmail(username);
        return loginWithUsername(request);
    }

    @PostMapping(value = "refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshRequest request){
        Optional<RefreshToken> token = refreshTokensService.renewToken(request.getRefreshToken());
        if(token.isEmpty())
            return ResponseEntity.badRequest().body(new ErrorResponse("Wrong refresh token"));

        ShopUser user = userDetailsService.getUserByUsername(token.get().getUsername());
        return ResponseEntity.ok().body(new LoginResponse(jwtUtils.generateToken(user),
                token.get().getValue()));
    }

    @PostMapping(value = "logout")
    public ResponseEntity<?> logout(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        this.refreshTokensService.deleteTokenByUsername((String) auth.getPrincipal());
        return ResponseEntity.ok().build();
    }


    private ResponseEntity<?> loginWithUsername(LoginRequest request){
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getUsernameOrEmail(), request.getPassword()));
        }
        catch(AuthenticationException e){
            return ResponseEntity.badRequest().body(new ErrorResponse("Wrong username/email or password"));
        }

        ShopUser user = userDetailsService.getUserByUsername(request.getUsernameOrEmail());
        RefreshToken refreshToken = refreshTokensService.newRefreshToken(user.getUsername());
        String jwtToken = jwtUtils.generateToken(user);

        return ResponseEntity.ok().body(new LoginResponse(jwtToken, refreshToken.getValue()));
    }
}
