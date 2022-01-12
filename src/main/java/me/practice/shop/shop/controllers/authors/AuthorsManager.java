package me.practice.shop.shop.controllers.authors;

import lombok.Getter;
import me.practice.shop.shop.controllers.authors.models.AuthorRequest;
import me.practice.shop.shop.controllers.authors.models.AuthorResponse;
import me.practice.shop.shop.controllers.authors.models.SimpleAuthor;
import me.practice.shop.shop.controllers.authors.models.SimpleAuthorsResponse;
import me.practice.shop.shop.database.authors.AuthorsRepository;
import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.models.Author;
import me.practice.shop.shop.models.BookProduct;
import me.practice.shop.shop.models.ErrorResponse;
import me.practice.shop.shop.utils.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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



    public List<Author> getAuthorsByNames(Collection<String> names){
       return IterableUtils.toList(this.authorsRepository.findAllByNames(names));
    }

    public ResponseEntity<?> updateAuthor(String id, AuthorRequest request){
        return this.authorsRepository.findById(id).map(oldAuthor->{
           Author newAuthor = new Author(id, request.getName(), request.getDescription());
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
        Query query = Query.query(Criteria.where("authorsNames").elemMatch(new Criteria().is(oldAuthor.getName())));
        UpdateDefinition updateDefinition = new Update().pull("authorsNames", oldAuthor.getName())
                .push("authorsNames", newAuthor.getName());
        this.mongoTemplate.updateMulti(query, updateDefinition, BookProduct.class);
    }

    public void checkForDeletingAuthors(BookProduct oldProduct){
        Collection<String> authorsIds = oldProduct.getAuthors().stream().map(Author::getId).collect(Collectors.toSet());
        System.out.println(authorsIds);
        Iterable<BookProduct> products = productsRepository.getByAuthorsIds(authorsIds);
        System.out.println(products);
        Collection<String> existingAuthorsIds = StreamSupport.stream(products.spliterator(), false)
                .flatMap(bookProduct -> bookProduct.getAuthors().stream().map(Author::getId))
                .collect(Collectors.toSet());
        authorsIds.removeAll(existingAuthorsIds);
        System.out.println(authorsIds);
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

}
