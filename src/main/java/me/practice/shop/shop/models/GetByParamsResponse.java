package me.practice.shop.shop.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;

@Data
@AllArgsConstructor
public class GetByParamsResponse<T> {
    private int pageNumber;
    private int totalPages;
    private long totalElements;
    private Collection<T> result;
}
