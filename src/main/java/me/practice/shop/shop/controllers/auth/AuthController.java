package me.practice.shop.shop.controllers.auth;

import me.practice.shop.shop.controllers.auth.models.*;
import me.practice.shop.shop.controllers.perms.models.RoleResponse;
import me.practice.shop.shop.database.users.UsersRepository;
import me.practice.shop.shop.models.ErrorResponse;
import me.practice.shop.shop.models.RefreshToken;
import me.practice.shop.shop.models.ResetPasswordToken;
import me.practice.shop.shop.models.ShopUser;
import me.practice.shop.shop.services.RefreshTokensService;
import me.practice.shop.shop.services.ResetTokensService;
import me.practice.shop.shop.services.UserDetailsServiceImpl;
import me.practice.shop.shop.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(value = "api/auth/")
public class AuthController {

    @Autowired
    private UsersRepository usersRepository;

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

    @Autowired
    private ResetTokensService resetTokensService;

    @PostMapping(value = "register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request){
        if(usersRepository.existsById(request.getUsername()))
            return ResponseEntity.badRequest().body(new ErrorResponse("Nazwa użytkownika zajęta"));
        if(usersRepository.existsByEmail(request.getEmail()))
            return ResponseEntity.badRequest().body(new ErrorResponse("Taki email już istnieje"));

        usersRepository.save(new ShopUser(request.getUsername(), request.getEmail(),
                encoder.encode(request.getPassword()), Collections.emptySet(), null));

        return loginWithUsername(new LoginRequest(request.getUsername(), request.getPassword()));
    }

    @PostMapping(value = "login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request){
        String username = usersRepository.findByEmail(request.getUsernameOrEmail())
                .map(ShopUser::getUsername).orElse(request.getUsernameOrEmail());

        request.setUsernameOrEmail(username);
        return loginWithUsername(request);
    }

    @PostMapping(value = "refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshRequest request){
        Optional<RefreshToken> token = refreshTokensService.getAndRenew(request.getRefreshToken());
        if(token.isEmpty())
            return ResponseEntity.badRequest().body(new ErrorResponse("Zły token"));

        ShopUser user = userDetailsService.getUserByUsername(token.get().getUsername());
        return ResponseEntity.ok().body(new LoginResponse(jwtUtils.generateToken(user),
                token.get().getValue().toString(), user.getRoles().stream().map(RoleResponse::new).collect(Collectors.toList())));
    }

    @PostMapping(value = "logout")
    public ResponseEntity<?> logout(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        this.refreshTokensService.deleteTokenByUsername((String) auth.getPrincipal());
        return ResponseEntity.ok().build();
    }


    @PostMapping(value="forgotPassword")
    public ResponseEntity<?> onForgotPassword(@Valid @RequestBody ForgotPasswordRequest request){
        Optional<ShopUser> user = this.usersRepository.findByEmail(request.getEmail());
        if(user.isEmpty())
            return ResponseEntity.badRequest().body(new ErrorResponse("Brak użytkownika o podanym emailu"));
        try {
            this.resetTokensService.generateTokenAndSendEmail(user.get().getUsername(), user.get().getEmail());
        }
        catch (MailException | MessagingException e){
            return ResponseEntity.badRequest().body(new ErrorResponse("Nie udało się wysłać emaila"));
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping(value="isResetTokenValid")
    public ResponseEntity<?> checkResetTokenValidity(@Valid @RequestBody ResetTokenValidationRequest request){
       return this.resetTokensService.tokenExist(request.getToken()) ?
               ResponseEntity.ok().build() : ResponseEntity.badRequest().body(
                       new ErrorResponse("Zły token"));
    }

    @PostMapping(value="resetPassword")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request){
        Optional<ResetPasswordToken> token = this.resetTokensService.getTokenByValue(request.getToken());
          if(token.isEmpty()) return ResponseEntity.badRequest().body(new ErrorResponse("Błędny token"));
        this.resetTokensService.deleteToken(request.getToken());
        return this.usersRepository.findById(token.get().getOwnerUsername())
                .map(user->{
                    user.setPassword(this.encoder.encode(request.getNewPassword()));
                    this.usersRepository.save(user);
                    return ResponseEntity.ok().build();
                }).orElse(ResponseEntity.badRequest().build());
    }

    private ResponseEntity<?> loginWithUsername(LoginRequest request){
//        System.out.println(this.usersRepository.findById(request.getUsernameOrEmail()));
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getUsernameOrEmail(), request.getPassword()));
        }
        catch(AuthenticationException e){
            return ResponseEntity.badRequest().body(new ErrorResponse("Wrong username/email or password"));
        }

        ShopUser user = userDetailsService.getUserByUsername(request.getUsernameOrEmail());
        RefreshToken refreshToken = refreshTokensService.createNewTokenOrRenew(user.getUsername());
        String jwtToken = jwtUtils.generateToken(user);

        return ResponseEntity.ok().body(new LoginResponse(jwtToken, refreshToken.getValue().toString(),
                user.getRoles().stream().map(RoleResponse::new).collect(Collectors.toList())));
    }
}
