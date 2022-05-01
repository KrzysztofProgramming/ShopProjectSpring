package me.practice.shop.shop.controllers.authors;

import me.practice.shop.shop.controllers.authors.models.AuthorRequest;
import me.practice.shop.shop.controllers.authors.models.AuthorResponse;
import me.practice.shop.shop.controllers.authors.models.GetAuthorsParams;
import me.practice.shop.shop.database.authors.AuthorsRepository;
import me.practice.shop.shop.models.Author;
import me.practice.shop.shop.models.ErrorResponse;
import me.practice.shop.shop.models.GetByParamsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping(value = "api/authors/")
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
public class AuthorsController {

    @Autowired
    private AuthorsManager authorsManager;

    @Autowired
    private AuthorsRepository authorsRepository;

    @GetMapping(value = "getAll")
    public ResponseEntity<?> getAuthors(@Valid GetAuthorsParams params){
        Page<AuthorResponse> authors = this.authorsManager.findAuthorsResponsesByParams(params);
        return ResponseEntity.ok(new GetByParamsResponse<>(authors.getNumber() + 1, authors.getTotalPages(),
                authors.getTotalElements(), authors.getContent()));
    }

    @GetMapping(value = "getSimpleList")
    public ResponseEntity<?> getSimpleAuthors(){
        return ResponseEntity.ok(this.authorsManager.getSimpleAuthorsList());
    }

    @GetMapping(value = "byId/{id}")
    public ResponseEntity<?> getAuthorById(@PathVariable Long id){
        Optional<AuthorResponse> author = this.authorsRepository.getAuthorResponseById(id);
        return author.isPresent() ? ResponseEntity.ok(author.get())
                : ResponseEntity.badRequest().body(this.authorsManager.getAuthorNotExistsInfo());
    }

    @PreAuthorize("hasAuthority('products:write')")
    @DeleteMapping(value = "deleteAuthor/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id){
        return this.authorsManager.deleteAuthor(id);
    }

    @PreAuthorize("hasAuthority('products:write')")
    @PostMapping(value = "newAuthor")
    public ResponseEntity<?> addNewAuthor(@Valid @RequestBody AuthorRequest request){
        try {
            return ResponseEntity.ok(new AuthorResponse(this.authorsRepository.save(this.newAuthor(request)),
                    0L));
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse("Autor o takim imieniu już istnieje"));
        }
    }

    @PreAuthorize("hasAuthority('products:write')")
    @PutMapping(value = "updateAuthor/{id}")
    public ResponseEntity<?> updateAuthor(@PathVariable Long id, @Valid @RequestBody AuthorRequest request){
        return this.authorsManager.updateAuthor(id, request);
    }


    private Author newAuthor(AuthorRequest request){
        return Author.builder().name(request.getName()).description(request.getDescription()).build();
    }

    private Author fromRequest(Long id, AuthorRequest request){
        return new Author(id, request.getName(), request.getDescription());
    }


}
