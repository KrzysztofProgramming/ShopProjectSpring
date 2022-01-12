package me.practice.shop.shop.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(CommonType.COLLECTION_NAME)
@AllArgsConstructor
public class CommonType {
    public static final String COLLECTION_NAME = "common_types_list";

    @Id
    private String name;
}
