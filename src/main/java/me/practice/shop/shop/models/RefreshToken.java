package me.practice.shop.shop.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document("refresh_token_database")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class RefreshToken {
    @Id
    @EqualsAndHashCode.Include
    private String value;
    @Indexed(unique = true)
    private String username;
    private Date issuedDate;
    private Date expireDate;

}
