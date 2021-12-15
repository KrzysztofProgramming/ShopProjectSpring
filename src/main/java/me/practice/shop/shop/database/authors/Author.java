package me.practice.shop.shop.database.authors;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@Data
@Document("authors_list")
public class Author {
    @Id
    private String id;

    @Indexed(unique = true)
    private String name;
}
