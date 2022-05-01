package me.practice.shop.shop.utils;

import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;

import java.util.Collection;

@SuppressWarnings("rawtypes")
public class CollectionSizeBridge implements ValueBridge<Collection, Integer> {
    @Override
    public Integer toIndexedValue(Collection value, ValueBridgeToIndexedValueContext context) {
        return value.size();
    }

    @Override
    public Integer parse(String value) {
        try{return Integer.parseInt(value);}
        catch (NumberFormatException e){return null;}
    }
}
