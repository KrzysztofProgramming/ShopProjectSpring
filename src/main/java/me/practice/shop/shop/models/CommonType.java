package me.practice.shop.shop.models;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Data
@Document(CommonType.COLLECTION_NAME)
public class CommonType {
    public static final String COLLECTION_NAME = "common_types_list";

    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private String name;

    private int productsCount;

    public CommonType(String name, int productsCount) {
        this.productsCount = productsCount;
        this.setName(name);
    }

    public void setName(String name) {
        this.name = toTypeName(name);
    }

    public static String toTypeName(String raw){
        if(raw.isEmpty()){
            return raw;
        }
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1).toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommonType that = (CommonType) o;
        return Objects.equals(name.toLowerCase(), that.name.toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }
}
