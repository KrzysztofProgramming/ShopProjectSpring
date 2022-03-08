package me.practice.shop.shop.controllers.products.events;

import lombok.Getter;
import me.practice.shop.shop.controllers.products.models.ProductRequest;
import me.practice.shop.shop.models.BookProduct;
import org.springframework.context.ApplicationEvent;


@Getter
public class BeforeProductUpdateEvent extends ApplicationEvent {
    private final BookProduct originalProduct;
    private final ProductRequest request;

    public BeforeProductUpdateEvent(Object source, ProductRequest request, BookProduct originalProduct) {
        super(source);
        this.originalProduct = originalProduct;
        this.request = request;
    }
}
