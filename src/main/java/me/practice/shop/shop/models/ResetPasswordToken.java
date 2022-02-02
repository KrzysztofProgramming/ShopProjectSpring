package me.practice.shop.shop.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document(ResetPasswordToken.COLLECTION_NAME)
public class ResetPasswordToken {
    public final static String COLLECTION_NAME = "reset_tokens_collection";

    @Id
    @EqualsAndHashCode.Include
    private String token;

    @Indexed(unique = true)
    private String ownerUsername;

    private Date issuedDate;
    private Date expireDate;
}
