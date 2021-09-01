package me.practice.shop.shop.permissions;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static me.practice.shop.shop.permissions.Permissions.*;

public enum Roles {
    ADMIN(PRODUCTS_MODIFY, USERS_MODIFY, USERS_GET),
    HELPER(PRODUCTS_MODIFY, USERS_GET),
    USER(),
    EMPTY();


    Roles(Permissions... authorities){
        this.authorities = Arrays.stream(authorities).map(Enum::name).collect(Collectors.toSet());
    }

    @Getter
    private final Collection<String> authorities;

    public static Roles fromString(String value){
        try{
            return Roles.valueOf(value);
        }
        catch(Exception e){
            return EMPTY;
        }
    }

    public static Collection<String> defaultRoles(){
        return Collections.singleton(USER.name());
    }

}
