package me.practice.shop.shop.controllers.products;

import me.practice.shop.shop.controllers.products.models.AuthorRequest;
import me.practice.shop.shop.database.authors.Author;
import me.practice.shop.shop.database.authors.AuthorsRepository;
import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.utils.IterableUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class AuthorsManager {

    @Autowired
    private AuthorsRepository authorsRepository;

    @Autowired
    private ProductsRepository productsRepository;


//    public boolean hasAuthorsChanged(Collection<Author> oldAuthors, Collection<AuthorRequest> newAuthors){
//
//    }

    public Optional<Collection<Author>> validateAndSaveAuthors(Collection<AuthorRequest> authors){
        if(authors.stream().anyMatch(authorRequest -> Strings.isEmpty(authorRequest.getId())
                && Strings.isEmpty(authorRequest.getName())))
            return Optional.empty();

        //getting Authors by ids
        List<String> ids = authors.stream()
                .filter(authorRequest -> Strings.isNotEmpty(authorRequest.getId()))
                .map(AuthorRequest::getId).collect(Collectors.toList());
        Iterable<Author> authorsByIds = Collections.emptyList();
        if(!ids.isEmpty()) {
            authorsByIds = this.authorsRepository.findAllById(ids);
            if (IterableUtils.size(authorsByIds) != ids.size())
                return Optional.empty();
        }

        //getting authors by names
        List<String> names = authors.stream()
                .filter(authorRequest -> Strings.isEmpty(authorRequest.getId())
                        && Strings.isNotEmpty(authorRequest.getName()))
                .map(AuthorRequest::getName).collect(Collectors.toList());
        Iterable<Author> authorsByNames = Collections.emptyList();
        Iterable<Author> newAuthors = Collections.emptyList();

        if(!names.isEmpty()) {
            authorsByNames = this.authorsRepository.findAllByNames(names);
            List<String> newAuthorsNames = new ArrayList<>(names);
            newAuthorsNames.removeAll(StreamSupport.stream(authorsByNames.spliterator(), false)
                    .map(Author::getName).collect(Collectors.toList()));
            //saving new authors
            if(!newAuthorsNames.isEmpty()){
               newAuthors = this.authorsRepository.saveAll(newAuthorsNames.stream().map(name ->
                        new Author(UUID.randomUUID().toString(), name)).collect(Collectors.toList()));
            }
        }

        Collection<Author> allAuthors = new LinkedList<>(IterableUtils.toList(authorsByIds));
        allAuthors.addAll(IterableUtils.toList(authorsByNames));
        allAuthors.addAll(IterableUtils.toList(newAuthors));


        return Optional.of(allAuthors);
    }

    public boolean deleteAuthor(String id){
       if(IterableUtils.size(this.productsRepository.getByAuthorId(id)) > 0){
           return false;
       }
       this.authorsRepository.deleteById(id);
       return true;
    }

}
