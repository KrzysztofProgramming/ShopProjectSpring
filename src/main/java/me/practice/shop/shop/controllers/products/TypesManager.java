package me.practice.shop.shop.controllers.products;

import me.practice.shop.shop.controllers.products.models.TypeResponse;
import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.database.products.types.CommonTypesRepository;
import me.practice.shop.shop.models.CommonType;
import me.practice.shop.shop.models.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TypesManager {
    @Autowired
    private CommonTypesRepository typesRepository;

    @Autowired
    private ProductsRepository productsRepository;



    public List<CommonType> addNewTypes(Collection<String> types){
        return this.typesRepository.saveAll(types.stream().map(CommonType::new).collect(Collectors.toSet()));
    }


    public ResponseEntity<?> addNewType(String type){
        try {
           return ResponseEntity.ok(new TypeResponse(typesRepository.save(new CommonType(type))));
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse("Taki typ już istnieje"));
        }
    }

    public Collection<String> getTypesAsStrings(){
        return this.typesRepository.getTypesNames();
    }

    public Collection<Long> getTypesIds(){
        return this.typesRepository.getTypesIds();
    }

    public Set<CommonType> getTypesByNames(Collection<String> names){
        return this.typesRepository.getAllByNames(names);
    }

    public ResponseEntity<?> updateType(Long id, String newName){
        try {
            if (this.typesRepository.updateName(id, newName) <= 0) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Taki typ nie istnieje"));
            }
            return ResponseEntity.ok().build();
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse("Typ z taką nazwą już istnieje"));
        }
    }

}
