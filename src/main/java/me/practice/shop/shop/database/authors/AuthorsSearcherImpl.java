package me.practice.shop.shop.database.authors;

import me.practice.shop.shop.database.Searcher;


public class AuthorsSearcherImpl extends Searcher {
//    @Autowired
//    private MongoTemplate mongoTemplate;
//
//    @Override
//    public Page<Author> findByParams(GetAuthorsParams params) {
//        Pageable pageable = PageRequest.of(params.getPageNumber() - 1, params.getPageSize(),
//                Sort.by("name").ascending());
//        Query query;
//        if(Strings.isNotBlank(params.getSearchPhrase())){
//            query = new TextQuery(params.getSearchPhrase()).sortByScore();
//        }
//        else {query = new Query().with(pageable);}
//        applyCriteria(query, maxMinCriteria("writtenBooks",
//                params.getMaxBooks(), params.getMinBooks()));
//
//        return PageableExecutionUtils.getPage(mongoTemplate.find(query, Author.class), pageable,
//                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Author.class));
//    }
}
