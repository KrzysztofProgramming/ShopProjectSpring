package me.practice.shop.shop.controllers.products.events;

import lombok.Getter;
import me.practice.shop.shop.controllers.products.models.ProductRequest;
import org.springframework.context.ApplicationEvent;

@Getter
public class BeforeProductCreateEvent extends ApplicationEvent {
    private final ProductRequest request;

    public BeforeProductCreateEvent(Object source, ProductRequest request) {
        super(source);
        this.request = request;
    }
}
