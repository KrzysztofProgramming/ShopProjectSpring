package me.practice.shop.shop.controllers.perms;

import me.practice.shop.shop.controllers.perms.models.RoleRequest;
import me.practice.shop.shop.controllers.perms.models.RoleResponse;
import me.practice.shop.shop.database.users.RolesRepository;
import me.practice.shop.shop.database.users.UsersDatabase;
import me.practice.shop.shop.models.ErrorResponse;
import me.practice.shop.shop.models.Role;
import me.practice.shop.shop.models.ShopUser;
import me.practice.shop.shop.permissions.Permissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RequestMapping(value = "api/perms/")
public class PermsController {

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private UsersDatabase usersDatabase;

    @PreAuthorize("hasAuthority('products:write')")
    @GetMapping(value = "roles")
    public ResponseEntity<?> getRoles(){
        return ResponseEntity.ok(this.rolesRepository.findAll(Sort.by(Sort.Direction.ASC, "order"))
                .stream().map(RoleResponse::new).collect(Collectors.toList()));
    }

    @PreAuthorize("hasAuthority('roles:write')")
    @PostMapping(value = "newRole")
    public ResponseEntity<?> createRoles(@Valid @RequestBody RoleRequest request){
        return this.ifUserHasPerms(request, user-> this.ifRequestValid(request, role-> ResponseEntity.ok(
                new RoleResponse(this.rolesRepository.save(role))
        )));
    }

    @PreAuthorize("hasAuthority('roles:write')")
    @PutMapping(value = "updateRole")
    public ResponseEntity<?> updateRole(@Valid @RequestBody RoleRequest request){
        return ifUserHasPerms(request, user-> this.ifRequestValid(request, role->
                ResponseEntity.ok(new RoleResponse(this.rolesRepository.save(role)))));
    }

    @PreAuthorize("hasAuthority('roles:write')")
    @DeleteMapping(value="deleteRole/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable String id){
        Optional<Role> role = this.rolesRepository.findById(id);
        if(role.isEmpty()) return ResponseEntity.badRequest().body(new ErrorResponse("Taka rola nie istenieje"));
        return ifUserHasPerms(new RoleRequest(role.get().getName(), role.get().getStrength(),
                Permissions.toNumber(role.get().getAuthorities())), user->{
            this.rolesRepository.deleteById(id);
            return ResponseEntity.ok().build();
        });
    }


    private ResponseEntity<?> ifUserHasPerms(RoleRequest request, Function<ShopUser, ResponseEntity<?>> fn){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth==null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Użytkownik niezalogowany"));
        }
        Optional<ShopUser> user = this.usersDatabase.findById((String) auth.getPrincipal());
        if(user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Użytkownik nie istnieje"));
        }
        Optional<Role> highestRole = user.get().getHighestRole();
        if(highestRole.isEmpty() || highestRole.get().getStrength() >= request.getAuthorities())
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Nie możesz modyfikować roli wyższej lub równej twojej"));

        return fn.apply(user.get());
    }

    private ResponseEntity<?> ifRequestValid(RoleRequest request, Function<Role, ResponseEntity<?>> fn){

        Set<String> authorities = Permissions.fromNumber(request.getAuthorities());
        if(!validateAuthorities(authorities)){
            return ResponseEntity.badRequest().body(new ErrorResponse("Błedne uprawniania"));
        }
        if(!validateByName(request.getName())){
            return ResponseEntity.badRequest().body(new ErrorResponse("Rola o takiej nazwie już istnieje"));
        }
        if(!validateByOrder(request.getOrder())){
            return ResponseEntity.badRequest().body(new ErrorResponse("Rola o podanym numerze już isteniej"));
        }
        return fn.apply(new Role(request.getName(), authorities, request.getOrder()));
    }

    private boolean validateAuthorities(Collection<String> authorities){
        return authorities.stream().map(Permissions::fromString).noneMatch(perm -> perm == Permissions.NO_PERMISSION);
    }

    private boolean validateByName(String name){
        return this.rolesRepository.findByName(name).isEmpty();
    }

    private boolean validateByOrder(double order){
        return this.rolesRepository.findByStrength(order).isEmpty();
    }
}
