package me.practice.shop.shop.database;


import javax.persistence.Query;

public class Searcher {
    public static Query applyParam(Query query, String name, Object value){
        if(value==null) return query;
        query.setParameter(name, value);
        return query;
    }
}
