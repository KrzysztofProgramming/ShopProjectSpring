package me.practice.shop.shop.database.products;

import me.practice.shop.shop.controllers.products.models.GetProductsParams;
import me.practice.shop.shop.models.BookProduct;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@SuppressWarnings("unused")
public class ProductsSearcherImpl implements ProductsSearcher {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<BookProduct> findByParams(GetProductsParams params) {
        Query query;
        if(Strings.isNotEmpty(params.getSearchPhrase())) {
            query = new TextQuery(params.getSearchPhrase()).sortByScore();
        }
        else{
            query = new Query();
        }
        applyCriteria(query, this.generatePriceCriteria(params));
        applyCriteria(query, this.generateStockCriteria(params));
        applyCriteria(query, this.generateTypesCriteria(params));

        Pageable pageable = PageRequest.of(params.getPageNumber() - 1, params.getPageSize());
        return PageableExecutionUtils.getPage(mongoTemplate.find(query, BookProduct.class), pageable,
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), BookProduct.class));
    }

    private Criteria generatePriceCriteria(GetProductsParams params){
        return params.getMinPrice() >=0 ?
                Criteria.where("price").gte(params.getMinPrice()) :
                params.getMaxPrice() >= 0 ?
                        Criteria.where("price").lte(params.getMaxPrice()) :
                        null;
    }

    private Query applyCriteria(Query query, CriteriaDefinition criteria){
        return criteria==null ? query : query.addCriteria(criteria);
    }

    private Criteria generateStockCriteria(GetProductsParams params){
        return params.getMinInStock() >=0 ?
                Criteria.where("inStock").gte(params.getMinInStock()) :
                params.getMaxInStock() >= 0 ?
                        Criteria.where("inStock").lte(params.getMaxInStock()) :
                        null;
    }

    private Criteria generateTypesCriteria(GetProductsParams params){
        return params.getTypes().isEmpty() ? null :
                Criteria.where("types").elemMatch(new Criteria().in(params.getTypes()));
    }

    private Date maxTime(Date d){
        Calendar c = new GregorianCalendar();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }
}
