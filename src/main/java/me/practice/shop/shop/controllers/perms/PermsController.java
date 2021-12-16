package me.practice.shop.shop.controllers.perms;

import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.database.users.RolesRepository;
import me.practice.shop.shop.permissions.Permissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RequestMapping(value = "api/perms/")
public class PermsController {

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @PreAuthorize("hasAuthority('roles:read')")
    @GetMapping(value = "getRoles")
    private ResponseEntity<?> getRoles(){
        System.out.println(this.rolesRepository);
        System.out.println(this.productsRepository);
        return ResponseEntity.ok(this.rolesRepository.findAll());
    }
//
//    @PreAuthorize("hasAuthority('roles:write')")
//    @PostMapping(value = "newRole")
//    private ResponseEntity<?> createRoles(@Valid @RequestBody RoleRequest request){
//        if(!validateAuthorities(request.getAuthorities())){
//            return ResponseEntity.badRequest().body(new ErrorResponse("Błedne uprawniania"));
//        }
//        try {   return ResponseEntity.ok(this.rolesRepository
//                .insert(new Role(request.getName(), request.getAuthorities()))); }
//        catch (DuplicateKeyException e){
//            return ResponseEntity.badRequest().body(new ErrorResponse("Rola o takiej nazwie już istnieje"));
//        }
//    }
//
//    @PreAuthorize("hasAuthority('roles:write')")
//    @PutMapping(value = "updateRole")
//    private ResponseEntity<?> updateRole(@Valid @RequestBody RoleRequest request){
//        if(!validateAuthorities(request.getAuthorities())){
//            return ResponseEntity.badRequest().body(new ErrorResponse("Błedne uprawniania"));
//        }
//        return ResponseEntity.ok(this.rolesRepository.save(new Role(request.getName(), request.getAuthorities())));
//    }
//
//    @DeleteMapping("hasAuthority('roles:write')")
//    @PutMapping(value="deleteRole/{id}")
//    private ResponseEntity<?> deleteRoles(@PathVariable String id){
//        this.rolesRepository.deleteById(id);
//        return ResponseEntity.ok().build();
//    }

    private boolean validateAuthorities(Collection<String> authorities){
        return authorities.stream().map(Permissions::fromString).noneMatch(perm -> perm == Permissions.NO_PERMISSION);
    }
}
