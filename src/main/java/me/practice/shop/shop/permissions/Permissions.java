package me.practice.shop.shop.permissions;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public enum Permissions {
    NO_PERMISSION(0, ""),
    PRODUCTS_WRITE(1, "products:write"),
    USERS_READ(1<<1, "users:read"),
    USERS_WRITE(1<<2, "users:write"),
    ROLES_WRITE(1<<3, "roles:write");

    @Getter
    private final long numberValue;
    private final String stringValue;


    Permissions(long numberValue, String stringValue) {
        this.numberValue = numberValue;
        this.stringValue = stringValue;
    }

    public static Permissions fromString(String name){
        return Arrays.stream(Permissions.values())
                .filter(value->value.stringValue.equalsIgnoreCase(name))
                .findAny().orElse(NO_PERMISSION);
    }

    public static Set<String> fromNumber(long value){
        return Arrays.stream(Permissions.values())
                .filter(perm -> perm.isInValue(value))
                .map(Permissions::toString)
                .collect(Collectors.toSet());
    }

    public static long allPerms(){
        return (1<<10) - 1;
    }

    public static long toNumber(Collection<String> permissions){
       return permissions.stream().map(perm -> Permissions.fromString(perm).getNumberValue())
               .reduce((aLong, aLong2) -> aLong | aLong2).orElse(0L);
    }

    private boolean isInValue(long value) {
        return (value & this.getNumberValue()) > 0;
    }

    @Override
    public String toString() {
        return this.stringValue;
    }
}
