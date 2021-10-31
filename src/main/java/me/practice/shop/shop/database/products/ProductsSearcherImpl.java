package me.practice.shop.shop.database.products;

import me.practice.shop.shop.controllers.products.models.GetProductsParams;
import me.practice.shop.shop.models.ShopProduct;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ProductsSearcherImpl<T> implements ProductsSearcher {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<ShopProduct> findByParams(GetProductsParams p) {
        Pageable pageable = PageRequest.of(p.getPageNumber() - 1, p.getPageSize());
        Query q;

        if(Strings.isNotEmpty(p.getSearchPhrase())){
            q = new TextQuery(p.getSearchPhrase()).sortByScore();
        }
        else{
            q = new Query();
        }
        q.with(pageable);

        Criteria c = Criteria.where("price");
        boolean addCriteria = false;
        if(p.getMinPrice() >= 0){
            c.gte(p.getMinPrice());
            addCriteria = true;
        }
        if(p.getMaxPrice() >= 0){
            c.lte(p.getMaxPrice());
            addCriteria = true;
        }
        if(addCriteria)
            q.addCriteria(c);


        if(!p.getTypes().isEmpty()){
            q.addCriteria(Criteria.where("types").elemMatch(new Criteria().in(p.getTypes())));
        }


        return PageableExecutionUtils.getPage(mongoTemplate.find(q, ShopProduct.class), pageable,
                () -> mongoTemplate.count(Query.of(q).limit(-1).skip(-1), ShopProduct.class));
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
