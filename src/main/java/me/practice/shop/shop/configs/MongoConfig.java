package me.practice.shop.shop.configs;

import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

public class MongoConfig extends AbstractMongoClientConfiguration {


    @Override
    protected String getDatabaseName() {
        return "spring_shop_database";
    }

    @Override
    protected boolean autoIndexCreation() {
        return true;
    }
}