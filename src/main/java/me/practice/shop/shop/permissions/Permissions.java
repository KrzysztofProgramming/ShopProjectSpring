package me.practice.shop.shop.permissions;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public enum Permissions {
    NO_PERMISSION(0),
    PRODUCTS_MODIFY(1),
    USERS_MODIFY(1<<1),
    USERS_GET(1<<2);

    @Getter
    private final long numberValue;

    Permissions(long value) {
        numberValue = value;
    }

    public static Permissions fromString(String name){
        try{
            return Permissions.valueOf(name);
        }
        catch(Exception e){
            return NO_PERMISSION;
        }
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
}
