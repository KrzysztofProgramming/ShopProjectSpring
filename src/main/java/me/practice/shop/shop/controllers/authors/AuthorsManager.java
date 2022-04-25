package me.practice.shop.shop.controllers.authors;

import lombok.Getter;
import me.practice.shop.shop.controllers.authors.models.AuthorRequest;
import me.practice.shop.shop.controllers.authors.models.AuthorResponse;
import me.practice.shop.shop.controllers.authors.models.SimpleAuthorsResponse;
import me.practice.shop.shop.database.authors.AuthorsRepository;
import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.models.Author;
import me.practice.shop.shop.models.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

@Component
public class AuthorsManager {
    @Getter
    private final ErrorResponse authorNotExistsInfo = new ErrorResponse("Brak autora o danym id");

    @Autowired
    private AuthorsRepository authorsRepository;

    @Autowired
    private ProductsRepository productsRepository;

    public Set<Author> getAuthorsByNames(Collection<String> names){
       return this.authorsRepository.findAllByNames(names);
    }

    public ResponseEntity<?> updateAuthor(Long id, AuthorRequest request){
        return this.authorsRepository.updateNameAndDescription(id, request.getName(), request.getDescription()) > 0 ?
            ResponseEntity.ok(new AuthorResponse(id, request.getName(), request.getDescription(),
                    this.authorsRepository.countAuthorBooks(id))) :
            ResponseEntity.badRequest().body("Autor o takiej nazwie już istnieje");
    }


    public ResponseEntity<?> deleteAuthor(Long id){
       if(this.productsRepository.countByAuthor(id) > 0){
            return ResponseEntity.badRequest().body("Niektóre produkty korzystają z tego autora, zmień to przed usunięciem go");
       }
       this.authorsRepository.deleteById(id);
       return ResponseEntity.ok().build();
    }

    public SimpleAuthorsResponse getSimpleAuthorsList(){
        return new SimpleAuthorsResponse(this.authorsRepository.getSimpleAuthorsList());
    }

    public Collection<String> getAuthorsNames(){
       return this.authorsRepository.getAuthorsNames();
    }

}
