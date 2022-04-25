package me.practice.shop.shop.database.files;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@Document
public class DatabaseImage {
    @Id
    private Long id;
    private String mediaType;
    private Binary image;
}
