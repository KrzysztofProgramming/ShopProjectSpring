package me.practice.shop.shop.database.orders;

import me.practice.shop.shop.controllers.users.models.profile.GetOrdersParams;
import me.practice.shop.shop.database.Searcher;
import me.practice.shop.shop.models.ShopOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;

public class OrdersSearcherImpl extends Searcher implements OrdersSearcher {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<ShopOrder> getByParams(GetOrdersParams params, String ownerUsername) {
        Query query = new Query();
        applyCriteria(query, this.generatePriceCriteria(params));
        applyCriteria(query, this.generateDateCriteria(params));
        applyCriteria(query, Criteria.where("ownerUsername").is(ownerUsername));
        Pageable pageable = PageRequest.of(params.getPageNumber() - 1, params.getPageSize());
        query.with(pageable);
        query.with(Sort.by(Sort.Direction.DESC, "issuedDate"));
        return PageableExecutionUtils.getPage(mongoTemplate.find(query, ShopOrder.class), pageable,
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), ShopOrder.class));
    }

    private Criteria generatePriceCriteria(GetOrdersParams params){
        return this.maxMinCriteria("totalPrice", params.getMaxPrice(), params.getMinPrice());
    }

    private Criteria generateDateCriteria(GetOrdersParams params){
        return this.maxMinCriteria("issuedDate", params.getMaxDate(), params.getMinDate());
    }
}
