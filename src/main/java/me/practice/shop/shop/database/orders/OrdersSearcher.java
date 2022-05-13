package me.practice.shop.shop.database.orders;

import me.practice.shop.shop.controllers.users.models.profile.GetOrdersParams;
import me.practice.shop.shop.database.ParamsApplicator;
import me.practice.shop.shop.models.ShopOrder;
import me.practice.shop.shop.utils.OrdersSortUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

@Component
public class OrdersSearcher {

    @Autowired
    private EntityManager entityManager;

    public Page<ShopOrder> getByParams(GetOrdersParams params, String ownerUsername){
        StringBuilder mainBuilder = new StringBuilder("SELECT o FROM ShopOrder o WHERE 1=1");
        StringBuilder countBuilder = new StringBuilder("SELECT COUNT(o) FROM ShopOrder o WHERE 1=1");
        List<StringBuilder> builders = List.of(mainBuilder, countBuilder);
        if(params.getMaxPrice()!=null)
            builders.forEach(builder->builder.append(" AND o.totalPrice <= :maxPrice"));
        if(params.getMinPrice()!=null)
            builders.forEach(builder->builder.append(" AND o.totalPrice >= :minPrice"));
        if(params.getMaxDate()!=null)
            builders.forEach(builder-> builder.append(" AND o.issuedDate <= :maxDate"));
        if(params.getMinDate()!=null)
            builders.forEach(builder->builder.append(" AND o.issuedDate >= :minDate"));
        if(params.getStatus()!=null)
            builders.forEach(builder->builder.append(" AND o.status = :status"));
        mainBuilder.append(OrdersSortUtils.getSort(params.getSort()));

        TypedQuery<ShopOrder> orderQuery = this.entityManager.createQuery(mainBuilder.toString(), ShopOrder.class);
        Query countQuery = this.entityManager.createQuery(countBuilder.toString());
        List<Query> queries = List.of(orderQuery, countQuery);
        queries.forEach(query -> query.setMaxResults(params.getPageSize()));
        queries.forEach(query->query.setFirstResult((params.getPageNumber() - 1) * params.getPageSize()));
        queries.forEach(query-> new ParamsApplicator(query)
                .applyParam("maxPrice", params.getMaxPrice())
                .applyParam("minPrice", params.getMinPrice())
                .applyParam("maxDate", params.getMaxDate())
                .applyParam("minDate", params.getMinDate())
                .applyParam("status", params.getStatus()));

       List<ShopOrder> orders = orderQuery.getResultList();
       long totalCount = (long) countQuery.getSingleResult();

        return PageableExecutionUtils.getPage(orders, PageRequest.of(params.getPageNumber() - 1, params.getPageSize()),
                ()->totalCount);
    }
}
