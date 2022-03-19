package me.practice.shop.shop.database.products;

import com.mongodb.bulk.BulkWriteResult;
import me.practice.shop.shop.controllers.products.models.GetProductsParams;
import me.practice.shop.shop.database.Searcher;
import me.practice.shop.shop.models.BookProduct;
import me.practice.shop.shop.utils.ProductsSortUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class ProductsSearcherImpl extends Searcher implements ProductsSearcher {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<BookProduct> findByParams(GetProductsParams params) {
        Query query;
        if(Strings.isNotEmpty(params.getSearchPhrase())) {
            query = new TextQuery(params.getSearchPhrase());
            if(ProductsSortUtils.isEmpty(params.getSort()))
                ((TextQuery)query).sortByScore();
        }
        else{
            query = new Query();
        }
        applyCriteria(query, this.generatePriceCriteria(params));
        applyCriteria(query, this.generateStockCriteria(params));
        applyCriteria(query, this.generateTypesCriteria(params));
        applyCriteria(query, this.generateAuthorsCriteria(params));
        Pageable pageable = PageRequest.of(params.getPageNumber() - 1, params.getPageSize());
        query.with(pageable);
        query.with(ProductsSortUtils.getSort(params.getSort()));
        return PageableExecutionUtils.getPage(mongoTemplate.find(query, BookProduct.class), pageable,
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), BookProduct.class));
    }

    @Override
    public boolean allExistByIds(Collection<String> ids) {
        return this.mongoTemplate.count(Query.query(Criteria.where("id").in(ids)), BookProduct.class) == ids.size();
    }

    @Override
    public BulkWriteResult decreaseProductsCounts(Map<String, Integer> productsCounts) {
        BulkOperations operation = this.mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, BookProduct.class);
        productsCounts.forEach((key, value) -> operation.updateOne(Query.query(Criteria.where("id").is(key)),
                new Update().inc("inStock", -value)));
        return operation.execute();
    }

    @Override
    public boolean allProductsAvailable(Map<String, Integer> amounts) {
        if(amounts.isEmpty()) return false;
        List<BookProduct> products= this.mongoTemplate.find(Query.query(Criteria.where("id").in(amounts.keySet())),
                BookProduct.class);
        return products.stream().allMatch(product->product.getInStock() >= amounts.get(product.getId()));
    }


    private Criteria generatePriceCriteria(GetProductsParams params){
        return maxMinCriteria("price", params.getMaxPrice(), params.getMinPrice());
    }

    private Criteria generateAuthorsCriteria(GetProductsParams params){
        return params.getAuthorsNames().size() == 0 ? null :
                Criteria.where("authorsNames").elemMatch(new Criteria().in(params.getAuthorsNames()));
    }

    private Criteria generateStockCriteria(GetProductsParams params){
        return maxMinCriteria("inStock", params.getMaxInStock(), params.getMinInStock());
    }

    private Criteria generateTypesCriteria(GetProductsParams params){
        return params.getTypes().isEmpty() ? null :
                Criteria.where("types").elemMatch(new Criteria().in(params.getTypes()));
    }
}
