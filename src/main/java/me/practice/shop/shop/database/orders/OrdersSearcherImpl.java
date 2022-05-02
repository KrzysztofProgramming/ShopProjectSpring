package me.practice.shop.shop.database.orders;

import me.practice.shop.shop.controllers.users.models.profile.GetOrdersParams;
import me.practice.shop.shop.database.Searcher;
import me.practice.shop.shop.models.ShopOrder;
import me.practice.shop.shop.utils.OrdersSortUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.support.PageableExecutionUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Collection;
import java.util.List;

public class OrdersSearcherImpl extends Searcher implements OrdersSearcher {
    @Autowired
    private EntityManager entityManager;


    @Override
    public Page<ShopOrder> getByParams(GetOrdersParams params, final String ownerUsername) {
        StringBuilder mainBuilder = new StringBuilder(
                "SELECT o FROM ShopOrder o WHERE o.ownerUsername = :username");
        StringBuilder countBuilder = new StringBuilder(
                "SELECT COUNT(o) FROM ShopOrder o WHERE o.ownerUsername = :username");
        Collection<StringBuilder> builders = List.of(mainBuilder, countBuilder);
        if(params.getStatus()!=null)
            builders.forEach(builder->builder.append(" AND o.status = :status"));
        if(params.getMaxDate()!=null)
            builders.forEach(builder -> builder.append(" AND o.issuedData <= :maxDate"));
        if(params.getMinDate()!=null)
            builders.forEach(builder->builder.append(" AND o.issuedData >= :minDate"));
        if(params.getMaxPrice()!=null)
            builders.forEach(builder -> builder.append(" AND o.totalPrice <= :maxPrice"));
        if(params.getMinPrice()!=null)
            builders.forEach(builder -> builder.append(" AND o.totalPrice >= :minPrice"));
        mainBuilder.append(OrdersSortUtils.getSort(params.getSort()));

        TypedQuery<ShopOrder> mainQuery = this.entityManager.createQuery(mainBuilder.toString(), ShopOrder.class);
        TypedQuery<Long> countQuery = this.entityManager.createQuery(countBuilder.toString(), Long.class);
        Collection<Query> queries = List.of(mainQuery, countQuery);

        queries.forEach(query->query.setParameter("username", ownerUsername));
        if(params.getStatus()!=null)
            queries.forEach(query->query.setParameter("status", params.getStatus()));
        if(params.getMaxDate()!=null)
            queries.forEach(query -> query.setParameter("maxDate", params.getMaxDate()));
        if(params.getMinDate()!=null)
            queries.forEach(query->query.setParameter("minData", params.getMinDate()));
        if(params.getMaxPrice()!=null)
            queries.forEach(query->query.setParameter("maxPrice", params.getMaxPrice()));
        if(params.getMinPrice()!=null)
            queries.forEach(query -> query.setParameter("minPrice", params.getMinPrice()));

        mainQuery.setFirstResult((params.getPageNumber() - 1) * params.getPageSize());
        mainQuery.setMaxResults(params.getPageSize());
        long totalElements = countQuery.getSingleResult();
        return PageableExecutionUtils.getPage(mainQuery.getResultList(),
                PageRequest.of(params.getPageNumber() - 1, params.getPageSize()),
                ()->totalElements);
    }

}
