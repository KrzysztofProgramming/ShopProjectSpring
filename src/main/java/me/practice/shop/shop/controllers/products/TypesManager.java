package me.practice.shop.shop.controllers.products;

import me.practice.shop.shop.controllers.products.models.GetTypesParams;
import me.practice.shop.shop.controllers.products.models.TypeDetailsResponse;
import me.practice.shop.shop.controllers.products.models.TypeResponse;
import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.database.products.types.CommonTypesRepository;
import me.practice.shop.shop.models.CommonType;
import me.practice.shop.shop.models.ErrorResponse;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.math.BigInteger;
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

    @Autowired
    private EntityManager entityManager;



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

    public Page<TypeDetailsResponse> findTypeResponsesByParams(GetTypesParams params){
        StringBuilder queryBuilder = new StringBuilder(
                "SELECT NEW me.practice.shop.shop.controllers.products.models.TypeDetailsResponse(" +
                        "t.id, t.name, COUNT(t)) " +
                        "FROM CommonType t JOIN t.books b GROUP BY t.id HAVING 1=1");
        StringBuilder counterBuilder = new StringBuilder(
                "SELECT COUNT(t) FROM (SELECT " +
                        "t.name, COUNT(t) " +
                        "FROM types_table t LEFT JOIN books_types b_t " +
                        "ON t.type_id = b_t.fk_type " +
                        "GROUP BY t.type_id HAVING 1=1"
        );
        Collection<StringBuilder> builders = List.of(queryBuilder, counterBuilder);

        if(params.getMaxBooks()!=null)
            builders.forEach(builder -> builder.append(" AND COUNT(t) <= :maxBooks"));
        if(params.getMinBooks()!=null)
            builders.forEach(builder -> builder.append(" AND COUNT(t) >= :minBooks"));
        if(Strings.isNotEmpty(params.getSearchPhrase()))
            builders.forEach(builder -> builder.append(" AND LOWER(t.name) LIKE :phrase"));
        queryBuilder.append(" ORDER BY t.name");
        counterBuilder.append(") t");

        TypedQuery<TypeDetailsResponse> resultQuery = this.entityManager.createQuery(queryBuilder.toString(),
                TypeDetailsResponse.class);
        Query counterQuery = this.entityManager.createNativeQuery(counterBuilder.toString());
        Collection<Query> queries = List.of(resultQuery, counterQuery);

        if(params.getMaxBooks()!=null)
            queries.forEach(query->query.setParameter("maxBooks",(long) params.getMaxBooks()));
        if(params.getMinBooks()!=null)
            queries.forEach(query->query.setParameter("minBooks",(long) params.getMinBooks()));
        if(Strings.isNotEmpty(params.getSearchPhrase()))
            queries.forEach(query->query.setParameter("phrase", params.getSearchPhrase().toLowerCase() + "%"));
        resultQuery.setFirstResult((params.getPageNumber() - 1) * params.getPageSize());
        resultQuery.setMaxResults(params.getPageSize());

        long totalCount = ((BigInteger) counterQuery.getSingleResult()).longValue();
        return PageableExecutionUtils.getPage(resultQuery.getResultList(),
                PageRequest.of(params.getPageNumber() - 1, params.getPageSize()),
                ()->totalCount);
    }

}
