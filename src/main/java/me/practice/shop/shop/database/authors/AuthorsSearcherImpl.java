package me.practice.shop.shop.database.authors;

import me.practice.shop.shop.controllers.authors.models.GetAuthorsParams;
import me.practice.shop.shop.models.Author;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.support.PageableExecutionUtils;


public class AuthorsSearcherImpl implements AuthorsSearcher {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<Author> findByParams(GetAuthorsParams params) {
        Pageable pageable = PageRequest.of(params.getPageNumber() - 1, params.getPageSize());
        if(Strings.isBlank(params.getSearchPhrase())){
            return PageableExecutionUtils.getPage(mongoTemplate.findAll(Author.class), pageable,
                    ()->mongoTemplate.estimatedCount(Author.class));
        }
        Query query = new TextQuery(params.getSearchPhrase()).sortByScore();
        return PageableExecutionUtils.getPage(mongoTemplate.find(query, Author.class), pageable,
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Author.class));
    }
}
