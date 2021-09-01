package me.practice.shop.shop.utils;

import lombok.Getter;

public enum ProductsTypes {
    ADVENTURE("ADVENTURE"),
    COMIC("COMIC"),
    DETECTIVE("DETECTIVE"),
    FANTASY("FANTASY"),
    HISTORICAL_FICTION("HISTORICAL FICTION"),
    HORROR("HORROR"),
    ROMANCE("ROMANCE"),
    SCI_FICTION("SCIENCE FICTION"),
    THRILLER("THRILLER"),
    BIOGRAPHIES("BIOGRAPHIES");

    @Getter
    private final String value;

    ProductsTypes(String value) {
        this.value = value;
    }
}
