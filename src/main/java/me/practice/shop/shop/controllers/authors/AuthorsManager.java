package me.practice.shop.shop.controllers.authors;

import lombok.Getter;
import me.practice.shop.shop.controllers.authors.models.AuthorRequest;
import me.practice.shop.shop.controllers.authors.models.AuthorResponse;
import me.practice.shop.shop.controllers.authors.models.SimpleAuthor;
import me.practice.shop.shop.controllers.authors.models.SimpleAuthorsResponse;
import me.practice.shop.shop.controllers.products.models.ProductAuthorRequest;
import me.practice.shop.shop.database.authors.AuthorsRepository;
import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.models.Author;
import me.practice.shop.shop.models.BookProduct;
import me.practice.shop.shop.models.ErrorResponse;
import me.practice.shop.shop.utils.IterableUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.*;
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

//    public boolean hasAuthorsChanged(Collection<Author> oldAuthors, Collection<AuthorRequest> newAuthors){
//
//    }

    public Optional<Collection<Author>> validateAndSaveAuthors(Collection<ProductAuthorRequest> requests){
        if(requests.stream().anyMatch(productAuthorRequest -> Strings.isEmpty(productAuthorRequest.getId())
                && Strings.isEmpty(productAuthorRequest.getName())))
            return Optional.empty();

        //getting Authors by ids
        List<String> ids = filterAuthorsWithIds(requests);
        Iterable<Author> authorsByIds = Collections.emptyList();
        if(!ids.isEmpty()) {
            authorsByIds = this.authorsRepository.findAllById(ids);
            if (IterableUtils.size(authorsByIds) != ids.size())
                return Optional.empty();
        }

        //getting authors by names
        List<Author> byNames = this.filterAuthorsWithNames(requests);

        List<Author> existingAuthorsByNames = Collections.emptyList();
        List<Author> newAuthors = Collections.emptyList();

        if(!byNames.isEmpty()) {
            existingAuthorsByNames = IterableUtils.toList(this.authorsRepository.findAllByNames(byNames.stream()
                    .map(Author::getName).collect(Collectors.toList())));
            final List<String> existingAuthorsNames = existingAuthorsByNames.stream().map(Author::getName).collect(Collectors.toList());
            newAuthors = new ArrayList<>(byNames);
            newAuthors.removeIf(author -> existingAuthorsNames.contains(author.getName()));

            try{ newAuthors = this.saveNewAuthors(newAuthors); }
            catch (IllegalArgumentException e) {return Optional.empty();}
        }

        Collection<Author> allAuthors = new LinkedList<>(IterableUtils.toList(authorsByIds));
        allAuthors.addAll(IterableUtils.toList(existingAuthorsByNames));
        allAuthors.addAll(IterableUtils.toList(newAuthors));


        return Optional.of(allAuthors);
    }

    private List<Author> saveNewAuthors(List<Author> newAuthors) throws IllegalArgumentException{
        if(!newAuthors.isEmpty()){
            if(this.hasNameRepetition(newAuthors)){
                throw new IllegalArgumentException("Duplicated new authors names");
            }
            return this.authorsRepository.saveAll(newAuthors.stream().map(author ->
                    new Author(UUID.randomUUID().toString(), author.getName(), author.getDescription()))
                    .collect(Collectors.toList()));
        }
        return newAuthors;
    }

    private boolean hasNameRepetition(Collection<Author> authors){
       return authors.stream().collect(Collectors.toCollection(()->
               new TreeSet<>(Comparator.comparing(Author::getName)))).size() != authors.size();
    }

    private List<Author> filterAuthorsWithNames(Collection<ProductAuthorRequest> requests){
        return requests.stream()
                .filter(productAuthorRequest -> Strings.isEmpty(productAuthorRequest.getId())
                        && Strings.isNotEmpty(productAuthorRequest.getName()))
                .map(productAuthorRequest -> new Author("", productAuthorRequest.getName(),
                        Strings.isNotEmpty(productAuthorRequest.getDescription()) ? productAuthorRequest.getDescription() : ""))
                .collect(Collectors.toList());
    }

    private List<String> filterAuthorsWithIds(Collection<ProductAuthorRequest> requests){
        return requests.stream()
                .filter(productAuthorRequest -> Strings.isNotEmpty(productAuthorRequest.getId()))
                .map(ProductAuthorRequest::getId).collect(Collectors.toList());
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
        Iterable<BookProduct> products = productsRepository.getByAuthorsIds(authorsIds);
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
