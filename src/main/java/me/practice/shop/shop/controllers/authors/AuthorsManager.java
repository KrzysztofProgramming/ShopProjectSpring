package me.practice.shop.shop.controllers.authors;

import com.mongodb.BasicDBObject;
import com.mongodb.DBRef;
import com.mongodb.client.DistinctIterable;
import lombok.Getter;
import me.practice.shop.shop.controllers.authors.models.AuthorRequest;
import me.practice.shop.shop.controllers.authors.models.AuthorResponse;
import me.practice.shop.shop.controllers.authors.models.SimpleAuthor;
import me.practice.shop.shop.controllers.authors.models.SimpleAuthorsResponse;
import me.practice.shop.shop.controllers.products.events.BeforeProductCreateEvent;
import me.practice.shop.shop.controllers.products.events.BeforeProductDeleteEvent;
import me.practice.shop.shop.controllers.products.events.BeforeProductUpdateEvent;
import me.practice.shop.shop.database.authors.AuthorsRepository;
import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.models.Author;
import me.practice.shop.shop.models.BookProduct;
import me.practice.shop.shop.models.ErrorResponse;
import me.practice.shop.shop.utils.IterableUtils;
import org.bson.Document;
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
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.count;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;

@Component
public class AuthorsManager {
    @Getter
    private ErrorResponse authorNotExistsInfo = new ErrorResponse("Brak autora o danym id");

    @Autowired
    private AuthorsRepository authorsRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @EventListener
    public void onProductUpdate(BeforeProductUpdateEvent event){
        List<String> newAuthors = new ArrayList<>();
        List<String> deletedAuthors = new ArrayList<>();
        event.getRequest().getAuthorsNames().stream().filter(name->
                !event.getOriginalProduct().getAuthorsNames().contains(name))
                .forEach(newAuthors::add);
        event.getOriginalProduct().getAuthorsNames().stream().filter(name->
                !event.getRequest().getAuthorsNames().contains(name))
                .forEach(deletedAuthors::add);
        this.updateWrittenBooks(newAuthors, deletedAuthors);
    }

    @EventListener
    public void onProductCreateEvent(BeforeProductCreateEvent event){
        this.incrementWrittenBooks(event.getRequest().getAuthorsNames());
    }

    @EventListener
    public void onProductDeleteEvent(BeforeProductDeleteEvent event){
        this.decrementWrittenBooks(event.getProduct().getAuthorsNames());
    }

    private void incrementWrittenBooks(Collection<String> authorsNames){
        this.mongoTemplate.updateMulti(Query.query(Criteria.where("name").in(authorsNames)),
                new Update().inc("writtenBooks", 1),Author.class);
    }

    private void decrementWrittenBooks(Collection<String> authorsNames){
        this.mongoTemplate.updateMulti(Query.query(Criteria.where("name").in(authorsNames)),
                new Update().inc("writtenBooks", -1),Author.class);
    }

    private void updateWrittenBooks(Collection<String> newAuthors, Collection<String> deletedAuthors){
        BulkOperations operations = this.mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Author.class);
        operations.updateMulti(Query.query(Criteria.where("name").in(deletedAuthors)),
                new Update().inc("writtenBooks", -1));
        operations.updateMulti(Query.query(Criteria.where("name").in(newAuthors)),
                new Update().inc("writtenBooks", 1));
        operations.execute();
    }

    public List<Author> getAuthorsByNames(Collection<String> names){
       return IterableUtils.toList(this.authorsRepository.findAllByNames(names));
    }

    public ResponseEntity<?> updateAuthor(String id, AuthorRequest request){
        return this.authorsRepository.findById(id).map(oldAuthor->{
           Author newAuthor = new Author(id, request.getName(), request.getDescription(), oldAuthor.getWrittenBooks());
           try {newAuthor = this.authorsRepository.save(newAuthor);}
           catch (Exception e){
               e.printStackTrace();
               return ResponseEntity.badRequest().body("Autor o takiej nazwie już istnieje");
           }
           if(!oldAuthor.getName().equals(request.getName()))
               this.updateAuthorNameInProducts(oldAuthor, newAuthor);
           return ResponseEntity.ok(new AuthorResponse(newAuthor));
        }).orElse(ResponseEntity.badRequest().body(this.authorNotExistsInfo));
    }

    private void updateAuthorNameInProducts(Author oldAuthor, Author newAuthor){
        Query query = Query.query(Criteria.where("authors").elemMatch(new Criteria().in(
                new DBRef(Author.COLLECTION_NAME, oldAuthor.getId()))));
        UpdateDefinition update = new Update().set("authorsNames.$[elem]", newAuthor.getName())
                .filterArray(Criteria.where("elem").is(oldAuthor.getName()));
//        BulkOperations operations = this.mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, BookProduct.class);
//        operations.updateMulti(query, new Update().pull("authorsNames", oldAuthor.getName()));
//        operations.updateMulti(query, new Update().push("authorsNames", newAuthor.getName()));
//        operations.execute();
        this.mongoTemplate.updateMulti(query, update, BookProduct.class);
    }

    public String recalcWrittenBooks(){
        List<SimpleAuthor> authors = this.getSimpleAuthorsList().getSimpleAuthors();
        FacetOperation facetOperation = Aggregation.facet();
        ProjectionOperation projectionOperation = Aggregation.project();
        for(SimpleAuthor author: authors){
            String id = author.getId();
            facetOperation = facetOperation.and(match(Criteria.where("authorsNames").in(author.getName())),
                    count().as(id)).as(id);
            projectionOperation = projectionOperation.and(context ->
                    new Document("$arrayElemAt", Arrays.asList("$"+id+"."+id, 0))).as(id);
        }
        Aggregation aggregation = Aggregation.newAggregation(facetOperation, projectionOperation);
        BasicDBObject result = this.mongoTemplate.aggregate(aggregation, BookProduct.class, BasicDBObject.class)
                .getUniqueMappedResult();
        Map map = result.toMap();
        BulkOperations bulkOperations = this.mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Author.class);
        map.forEach((key, entry)-> bulkOperations.updateOne(Query.query(Criteria.where("id").is(key)),
                Update.update("writtenBooks", entry)));
        bulkOperations.execute();
        return result.toString();
    }

    public void checkForDeletingAuthors(BookProduct oldProduct){
        Collection<String> authorsIds = oldProduct.getAuthors().stream().map(Author::getId).collect(Collectors.toSet());
        Iterable<BookProduct> products = productsRepository.getByAuthorsIds(authorsIds.stream()
                .map(id-> new DBRef(Author.COLLECTION_NAME, id)).collect(Collectors.toList()));
        Collection<String> existingAuthorsIds = StreamSupport.stream(products.spliterator(), false)
                .flatMap(bookProduct -> bookProduct.getAuthors().stream().map(Author::getId))
                .collect(Collectors.toSet());
        authorsIds.removeAll(existingAuthorsIds);
        if(authorsIds.isEmpty()) return;
        this.authorsRepository.deleteAllById(authorsIds);
    }

    public ResponseEntity<?> deleteAuthor(String id){
       if(IterableUtils.size(this.productsRepository.getByAuthorId(id)) > 0){
            return ResponseEntity.badRequest().body("Niektóre produkty korzystają z tego autora, zmień to przed usunięciem go");
       }
       this.authorsRepository.deleteById(id);
       return ResponseEntity.ok().build();
    }

    public SimpleAuthorsResponse getSimpleAuthorsList(){
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.project(SimpleAuthor.class));
        return new SimpleAuthorsResponse(this.mongoTemplate.aggregate(aggregation, Author.class, SimpleAuthor.class)
                .getMappedResults());
    }

    public DistinctIterable<String> getAuthorsNames(){
       return this.mongoTemplate.getCollection(Author.COLLECTION_NAME).distinct("name", String.class);
    }

}
