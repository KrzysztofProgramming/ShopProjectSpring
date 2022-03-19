package me.practice.shop.shop.database.authors;

import me.practice.shop.shop.controllers.authors.models.GetAuthorsParams;
import me.practice.shop.shop.database.Searcher;
import me.practice.shop.shop.models.Author;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.support.PageableExecutionUtils;


public class AuthorsSearcherImpl extends Searcher implements AuthorsSearcher {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<Author> findByParams(GetAuthorsParams params) {
        Pageable pageable = PageRequest.of(params.getPageNumber() - 1, params.getPageSize(),
                Sort.by("name").ascending());
        Query query;
        if(Strings.isNotBlank(params.getSearchPhrase())){
            query = new TextQuery(params.getSearchPhrase()).sortByScore();
        }
        else {query = new Query().with(pageable);}
        applyCriteria(query, maxMinCriteria("writtenBooks",
                params.getMaxBooks(), params.getMinBooks()));

        return PageableExecutionUtils.getPage(mongoTemplate.find(query, Author.class), pageable,
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Author.class));
    }
}
