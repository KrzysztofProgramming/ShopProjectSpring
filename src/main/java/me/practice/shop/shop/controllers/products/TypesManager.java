package me.practice.shop.shop.controllers.products;

import com.mongodb.BasicDBObject;
import com.mongodb.client.DistinctIterable;
import me.practice.shop.shop.controllers.products.events.BeforeProductCreateEvent;
import me.practice.shop.shop.controllers.products.events.BeforeProductDeleteEvent;
import me.practice.shop.shop.controllers.products.events.BeforeProductUpdateEvent;
import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.database.products.types.CommonTypesRepository;
import me.practice.shop.shop.models.BookProduct;
import me.practice.shop.shop.models.CommonType;
import me.practice.shop.shop.models.ErrorResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.FacetOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.count;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;

@Component
public class TypesManager {
    @Autowired
    private CommonTypesRepository typesRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @EventListener
    public void onProductCreate(BeforeProductCreateEvent event){
        this.incrementProductsCounter(event.getRequest().getTypes());
    }

    @EventListener
    public void onProductUpdate(BeforeProductUpdateEvent event){
        List<String> newTypes = new ArrayList<>();
        List<String> deletedTypes = new ArrayList<>();
        for(String type: event.getRequest().getTypes()){
            if(!event.getOriginalProduct().getTypes().contains(type))
                newTypes.add(type);
        }
        for(String type: event.getOriginalProduct().getTypes()){
            if(!event.getRequest().getTypes().contains(type)){
                deletedTypes.add(type);
            }
        }
        this.updateTypeCounts(newTypes, deletedTypes);
    }

    @EventListener
    public void onProductDelete(BeforeProductDeleteEvent event){
        this.decrementProductsCounter(event.getProduct().getTypes());
    }

    public List<CommonType> addNewTypes(Collection<String> types){
        return this.typesRepository.saveAll(types.stream().map(name-> new CommonType(name, 0)).collect(Collectors.toSet()));
    }

    public String recalcTypesCounters(){
        List<CommonType> types = this.typesRepository.findAll();
        FacetOperation facetOperation = Aggregation.facet();
        ProjectionOperation projectionOperation = Aggregation.project();
        for(CommonType type: types){
            String id = type.getId().toHexString();
            facetOperation = facetOperation.and(match(Criteria.where("types").in(type.getName())),
                    count().as(id)).as(id);
            projectionOperation = projectionOperation.and(context ->
                    new Document("$arrayElemAt", Arrays.asList("$"+id+"."+id, 0))).as(id);
        }
        Aggregation aggregation = Aggregation.newAggregation(facetOperation, projectionOperation);
        BasicDBObject result = this.mongoTemplate.aggregate(aggregation, BookProduct.class, BasicDBObject.class)
                .getUniqueMappedResult();
        Map map = result.toMap();
        BulkOperations bulkOperations = this.mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, CommonType.class);
        map.forEach((key, entry)-> bulkOperations.updateOne(Query.query(Criteria.where("id").is(key)),
                Update.update("productsCount", entry)));
        bulkOperations.execute();
        return result.toString();
    }

    public void incrementProductsCounter(Collection<String> types){
        this.mongoTemplate.updateMulti(Query.query(Criteria.where("name").in(types)),
                new Update().inc("productsCount", 1), CommonType.class);
    }

    public void decrementProductsCounter(Collection<String> types){
        BulkOperations operation = this.mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, CommonType.class);
        operation.updateMulti(Query.query(Criteria.where("name").in(types)),
                new Update().inc("productsCount", -1));
        operation.remove(Query.query(Criteria.where("productCount").lte(0)));
        operation.execute();
    }

    public void updateTypeCounts(Collection<String> newTypes, Collection<String> deletedTypes){
        BulkOperations operation = this.mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, CommonType.class);
        operation.updateMulti(Query.query(Criteria.where("name").in(deletedTypes)),
                new Update().inc("productsCount", -1));
        operation.updateMulti(Query.query(Criteria.where("name").in(newTypes)),
                new Update().inc("productsCount", 1));
        operation.remove(Query.query(Criteria.where("productCount").lte(0)));
        operation.execute();
    }

    public ResponseEntity<?> addNewType(String type){
        try {
           return ResponseEntity.ok(typesRepository.insert(new CommonType(type, 0)));
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse("Taki typ już istnieje"));
        }
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

    public DistinctIterable<String> getTypesAsStrings(){
        return this.mongoTemplate.getCollection(CommonType.COLLECTION_NAME).distinct("name", String.class);
    }

    public DistinctIterable<ObjectId> getTypesIds(){
        return this.mongoTemplate.getCollection(CommonType.COLLECTION_NAME).distinct("_id", ObjectId.class);
    }

    public List<String> getTypesByNames(Collection<String> names){
        return StreamSupport.stream(this.typesRepository.getAllByNames(names.stream()
                .map(CommonType::toTypeName).collect(Collectors.toList())).spliterator(), false)
                .map(CommonType::getName).collect(Collectors.toList());
    }

    public ResponseEntity<?> updateType(String name, String newName){
        if(this.mongoTemplate.exists(Query.query(Criteria.where("name").is(newName)), CommonType.class)){
            return ResponseEntity.badRequest().body(new ErrorResponse("Produkt z taką nazwą już istnieje"));
        }
        this.mongoTemplate.updateFirst(Query.query(Criteria.where("name").is(name)), Update.update("name", newName),
                CommonType.class);
        return ResponseEntity.ok().build();
    }

}
