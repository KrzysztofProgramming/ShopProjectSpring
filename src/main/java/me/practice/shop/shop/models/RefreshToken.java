package me.practice.shop.shop.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document("refresh_token_database")
@Data
@AllArgsConstructor
public class RefreshToken {
    @Id
    private String value;
    @Indexed(unique = true)
    private String username;
    private Date issuedDate;
    private Date expireDate;

}
