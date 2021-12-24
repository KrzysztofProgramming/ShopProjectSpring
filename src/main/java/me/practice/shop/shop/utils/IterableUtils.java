package me.practice.shop.shop.utils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class IterableUtils {
    private IterableUtils() {}

    public static int size(Iterable<?> data) {

        if (data instanceof Collection) {
            return ((Collection<?>) data).size();
        }
        int counter = 0;
        for (Object i : data) {
            counter++;
        }
        return counter;
    }

    public static <T> List<T> toList(Iterable<T> data){
        if(data instanceof List){
            return (List<T>)data;
        }
        return StreamSupport.stream(data.spliterator(), false).collect(Collectors.toList());
    }
}
