package me.practice.shop.shop.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.logging.log4j.util.Strings;

import javax.persistence.Query;
import java.util.Collection;


@AllArgsConstructor
@Data
public class ParamsApplicator {
    private Query query;

    public ParamsApplicator applyParam(String name, Object value){
        if(value == null
                || (value instanceof Collection && ((Collection<?>)value).size() <= 0)
        || (value instanceof String) && Strings.isEmpty((String) value)) return this;
        this.query.setParameter(name, value);
        return this;
    }
}
