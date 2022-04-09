package me.practice.shop.shop.database.orders;

import me.practice.shop.shop.controllers.users.models.profile.GetOrdersParams;
import me.practice.shop.shop.database.Searcher;
import me.practice.shop.shop.models.ShopOrder;
import me.practice.shop.shop.utils.OrdersSortUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.Calendar;
import java.util.Date;

public class OrdersSearcherImpl extends Searcher implements OrdersSearcher {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<ShopOrder> getByParams(GetOrdersParams params, String ownerUsername) {
        Query query = new Query();
        applyCriteria(query, this.generatePriceCriteria(params));
        applyCriteria(query, this.generateDateCriteria(params));
        applyCriteria(query, Criteria.where("ownerUsername").is(ownerUsername));
        applyCriteria(query, this.generateStatusCriteria((params)));
        Pageable pageable = PageRequest.of(params.getPageNumber() - 1, params.getPageSize());
        query.with(pageable);
        query.with(OrdersSortUtils.getSort(params.getSort()));
        return PageableExecutionUtils.getPage(mongoTemplate.find(query, ShopOrder.class), pageable,
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), ShopOrder.class));
    }

    private Criteria generatePriceCriteria(GetOrdersParams params){
        return maxMinCriteria("totalPrice", params.getMaxPrice(), params.getMinPrice());
    }

    private Criteria generateDateCriteria(GetOrdersParams params){
        return maxMinCriteria("issuedDate", this.maxDate(params.getMaxDate()), params.getMinDate());
    }

    private Criteria generateStatusCriteria(GetOrdersParams params){
        return params.getStatus()!=null ? Criteria.where("status").is(params.getStatus()) : null;
    }

    private Date maxDate(Date date){
        if(date==null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        return cal.getTime();
    }
}
