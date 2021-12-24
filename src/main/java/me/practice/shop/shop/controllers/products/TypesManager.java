package me.practice.shop.shop.controllers.products;

import me.practice.shop.shop.database.products.CommonTypesRepository;
import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.models.BookProduct;
import me.practice.shop.shop.models.CommonType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class TypesManager {
    @Autowired
    private CommonTypesRepository typesRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public void addNewTypes(Collection<String> types){
        this.typesRepository.saveAll(types.stream().map(CommonType::new).collect(Collectors.toSet()));
    }

    public void checkForRemovingTypes(BookProduct oldProduct){
        Collection<String> types = new HashSet<>(oldProduct.getTypes());
        Iterable<BookProduct> products = productsRepository.getByTypes(types);
        Collection<String> existingTypes = StreamSupport.stream(products.spliterator(), false)
                .flatMap(bookProduct -> bookProduct.getTypes().stream())
                .collect(Collectors.toSet());
        types.removeAll(existingTypes);
        System.out.println(types);
        if(types.isEmpty()) return;
        this.typesRepository.deleteAllById(types);
    }

    public Collection<String> getTypesAsStrings(){
        return this.typesRepository.findAll().stream().map(CommonType::getName).collect(Collectors.toList());
    }


}
