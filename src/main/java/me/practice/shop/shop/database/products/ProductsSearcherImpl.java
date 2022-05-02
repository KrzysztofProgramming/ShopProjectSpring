package me.practice.shop.shop.database.products;

import me.practice.shop.shop.controllers.products.models.GetProductsParams;
import me.practice.shop.shop.database.Searcher;
import me.practice.shop.shop.models.BookProduct;
import me.practice.shop.shop.utils.ProductsSortUtils;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.scope.SearchScope;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.support.PageableExecutionUtils;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@SuppressWarnings("unused")
public class ProductsSearcherImpl extends Searcher implements ProductsSearcher {
    @Autowired
    private EntityManager entityManager;

    @Override
    @Transactional
    public Page<BookProduct> findByParams(GetProductsParams params) {
        SearchSession searchSession = Search.session(this.entityManager);
        SearchScope<BookProduct> scope = searchSession.scope(BookProduct.class);
        SearchResult<BookProduct> result = searchSession.search(BookProduct.class).where(f-> {
            var filter = f.bool()
                    .must(f.range().field("price").between(params.getMinPrice(), params.getMaxPrice()))
                    .must(f.range().field("inStock").between(params.getMinInStock(), params.getMaxInStock()));
            if(params.getIsArchived()!=null)
                filter = filter.must(f.match().fields("isArchived").matching(params.getIsArchived()));
            if(params.getAuthors().size() > 0)
                filter = filter.must(f.terms().field("authors.id").matchingAny(params.getAuthors()));
            if(params.getTypes().size() > 0)
                filter = filter.must(f.terms().field("types.id").matchingAny(params.getTypes()));
            if(Strings.isNotEmpty(params.getSearchPhrase()))
                filter = filter.must(f.match().fields("name", "authors.name", "types.name", "description")
                        .matching(params.getSearchPhrase()));
          return filter;
        }).sort(f-> ProductsSortUtils.getSort(f, params.getSort()))
                .fetch((params.getPageNumber()-1) * params.getPageSize(), params.getPageSize());
        long totalCount = result.total().hitCount();
        List<BookProduct> books = result.hits();

        return PageableExecutionUtils.getPage(books,
                PageRequest.of(params.getPageNumber() - 1, params.getPageSize()),
                ()->totalCount);
    }
//    @Autowired
//    private MongoTemplate mongoTemplate;
//
//    @Override
//    public Page<BookProduct> findByParams(GetProductsParams params) {
//        Query query;
//        if(Strings.isNotEmpty(params.getSearchPhrase())) {
//            query = new TextQuery(params.getSearchPhrase());
//            if(ProductsSortUtils.isEmpty(params.getSort()))
//                ((TextQuery)query).sortByScore();
//        }
//        else{
//            query = new Query();
//        }
//        applyCriteria(query, this.generatePriceCriteria(params));
//        applyCriteria(query, this.generateStockCriteria(params));
//        applyCriteria(query, this.generateTypesCriteria(params));
//        applyCriteria(query, this.generateAuthorsCriteria(params));
//        Pageable pageable = PageRequest.of(params.getPageNumber() - 1, params.getPageSize());
//        query.with(pageable);
//        query.with(ProductsSortUtils.getSort(params.getSort()));
//        return PageableExecutionUtils.getPage(mongoTemplate.find(query, BookProduct.class), pageable,
//                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), BookProduct.class));
//    }
//
//    @Override
//    public boolean allExistByIds(Collection<String> ids) {
//        return this.mongoTemplate.count(Query.query(Criteria.where("id").in(ids)), BookProduct.class) == ids.size();
//    }
//
//    @Override
//    public BulkWriteResult decreaseProductsCounts(Map<String, Integer> productsCounts) {
//        BulkOperations operation = this.mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, BookProduct.class);
//        productsCounts.forEach((key, value) -> operation.updateOne(Query.query(Criteria.where("id").is(key)),
//                new Update().inc("inStock", -value)));
//        return operation.execute();
//    }
//
//    @Override
//    public boolean allProductsAvailable(Map<String, Integer> amounts) {
//        if(amounts.isEmpty()) return false;
//        List<BookProduct> products= this.mongoTemplate.find(Query.query(Criteria.where("id").in(amounts.keySet())),
//                BookProduct.class);
//        return products.stream().allMatch(product->product.getInStock() >= amounts.get(product.getId()));
//    }
//
//
//    private Criteria generatePriceCriteria(GetProductsParams params){
//        return maxMinCriteria("price", params.getMaxPrice(), params.getMinPrice());
//    }
//
//    private Criteria generateAuthorsCriteria(GetProductsParams params){
//        return params.getAuthorsNames().size() == 0 ? null :
//                Criteria.where("authorsNames").elemMatch(new Criteria().in(params.getAuthorsNames()));
//    }
//
//    private Criteria generateStockCriteria(GetProductsParams params){
//        return maxMinCriteria("inStock", params.getMaxInStock(), params.getMinInStock());
//    }
//
//    private Criteria generateTypesCriteria(GetProductsParams params){
//        return params.getTypes().isEmpty() ? null :
//                Criteria.where("types").elemMatch(new Criteria().in(params.getTypes()));
//    }
}
