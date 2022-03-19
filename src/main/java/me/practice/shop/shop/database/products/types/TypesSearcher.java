package me.practice.shop.shop.database.products.types;

import me.practice.shop.shop.controllers.products.models.GetTypesParams;
import me.practice.shop.shop.models.CommonType;
import org.springframework.data.domain.Page;

public interface TypesSearcher {
    Page<CommonType> findByParams(GetTypesParams params);
}
