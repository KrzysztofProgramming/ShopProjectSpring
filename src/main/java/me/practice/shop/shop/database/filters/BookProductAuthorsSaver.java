package me.practice.shop.shop.database.filters;

import me.practice.shop.shop.models.BookProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
public class BookProductAuthorsSaver extends AbstractMongoEventListener<BookProduct> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<BookProduct> event) {
        BookProduct product = event.getSource();

        super.onBeforeConvert(event);
    }
}
