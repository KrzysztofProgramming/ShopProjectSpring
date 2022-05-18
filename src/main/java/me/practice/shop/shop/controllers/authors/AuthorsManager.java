package me.practice.shop.shop.controllers.authors;

import lombok.Getter;
import me.practice.shop.shop.controllers.authors.models.AuthorRequest;
import me.practice.shop.shop.controllers.authors.models.AuthorResponse;
import me.practice.shop.shop.controllers.authors.models.GetAuthorsParams;
import me.practice.shop.shop.controllers.authors.models.SimpleAuthorsResponse;
import me.practice.shop.shop.database.authors.AuthorsRepository;
import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.models.Author;
import me.practice.shop.shop.models.ErrorResponse;
import me.practice.shop.shop.utils.AuthorsSortUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
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

@Component
public class AuthorsManager {
    @Getter
    private final ErrorResponse authorNotExistsInfo = new ErrorResponse("Brak autora o danym id");

    @Autowired
    private AuthorsRepository authorsRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private EntityManager entityManager;

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
        try {
            this.authorsRepository.deleteById(id);
        }
        catch(EmptyResultDataAccessException ignore){}
       return ResponseEntity.ok().build();
    }

    public SimpleAuthorsResponse getSimpleAuthorsList(){
        return new SimpleAuthorsResponse(this.authorsRepository.getSimpleAuthorsList());
    }

    public Page<AuthorResponse> findAuthorsResponsesByParams(GetAuthorsParams params){
        StringBuilder queryBuilder = new StringBuilder(
                "SELECT NEW me.practice.shop.shop.controllers.authors.models.AuthorResponse(" +
                "a.id, a.name, a.description, COUNT(b)) " +
                "FROM Author a LEFT JOIN a.books b GROUP BY a.id HAVING 1=1");
        StringBuilder counterBuilder = new StringBuilder(
                "SELECT COUNT(a) FROM (SELECT " +
                        "a.name, COUNT(b_a) as written_books " +
                        "FROM authors_table a LEFT JOIN books_authors b_a " +
                        "ON a.author_id = b_a.author_id " +
                        "GROUP BY a.author_id HAVING 1=1"
        );
        Collection<StringBuilder> builders = List.of(queryBuilder, counterBuilder);

        if(params.getMaxBooks()!=null)
            builders.forEach(builder -> builder.append(" AND COUNT(a) <= :maxBooks"));
        if(params.getMinBooks()!=null)
            builders.forEach(builder -> builder.append(" AND COUNT(a) >= :minBooks"));
        if(Strings.isNotEmpty(params.getSearchPhrase()))
            builders.forEach(builder -> builder.append(" AND LOWER(a.name) LIKE :phrase"));
        queryBuilder.append(AuthorsSortUtils.getSort(params.getSort()));
        counterBuilder.append(") a");

        TypedQuery<AuthorResponse> resultQuery = this.entityManager.createQuery(queryBuilder.toString(), AuthorResponse.class);
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

    public Collection<String> getAuthorsNames(){
       return this.authorsRepository.getAuthorsNames();
    }

}

