package me.practice.shop.shop.database;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;

public class Searcher {
    public static Query applyCriteria(Query query, CriteriaDefinition criteria){
        return criteria==null ? query : query.addCriteria(criteria);
    }

    public static Criteria maxMinCriteria(String fieldName, Object maxValue, Object minValue){
        if(maxValue==null && minValue == null) return null;
        Criteria criteria = Criteria.where(fieldName);
        if(maxValue != null) criteria.lte(maxValue);
        if(minValue != null) criteria.gte(minValue);
        return criteria;
    }
}
