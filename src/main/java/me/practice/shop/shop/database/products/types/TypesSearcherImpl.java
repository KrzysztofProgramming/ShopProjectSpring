package me.practice.shop.shop.database.products.types;

import me.practice.shop.shop.database.Searcher;

public class TypesSearcherImpl extends Searcher {

//    @Autowired
//    private MongoTemplate mongoTemplate;
//
//    @Override
//    public Page<CommonType> findByParams(GetTypesParams params) {
//        Pageable pageable = PageRequest.of(params.getPageNumber() - 1, params.getPageSize(),
//                Sort.by("name").ascending());
//        Query query;
//        if(Strings.isNotBlank(params.getSearchPhrase())){
//            query = new TextQuery(params.getSearchPhrase()).sortByScore();
//        }
//        else {query = new Query().with(pageable);}
//        applyCriteria(query, maxMinCriteria("productsCount",
//                params.getMaxProducts(), params.getMinProducts()));
//
//        return PageableExecutionUtils.getPage(mongoTemplate.find(query, CommonType.class), pageable,
//                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), CommonType.class));
//    }
}
