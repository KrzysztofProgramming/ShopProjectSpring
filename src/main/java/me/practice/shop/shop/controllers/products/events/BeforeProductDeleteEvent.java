package me.practice.shop.shop.controllers.products.events;

import lombok.Getter;
import me.practice.shop.shop.models.BookProduct;
import org.springframework.context.ApplicationEvent;

@Getter
public class BeforeProductDeleteEvent extends ApplicationEvent {
    private final BookProduct product;

    public BeforeProductDeleteEvent(Object source, BookProduct product) {
        super(source);
        this.product = product;
    }
}
