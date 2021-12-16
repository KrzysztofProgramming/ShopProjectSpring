package me.practice.shop.shop.permissions;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public enum Permissions {
    NO_PERMISSION(0, ""),
    PRODUCTS_WRITE(1, "products:write"),
    USERS_WRITE(1<<1, "users:write"),
    USERS_READ(1<<2, "users:read"),
    ROLES_READ(1<<3, "roles:read"),
    ROLES_WRITE(1<<4, "roles:write");

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

    public static Collection<Permissions> fromNumber(long value){
        return Arrays.stream(Permissions.values())
                .filter(perm -> perm.isInValue(value))
                .collect(Collectors.toSet());
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
