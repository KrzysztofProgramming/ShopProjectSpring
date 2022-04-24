package me.practice.shop.shop.models;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Builder
@Entity
@Table(name = ResetPasswordToken.TABLE_NAME,
        indexes = @Index(name = "index_owner_username", columnList = "ownerUsername"))
public class ResetPasswordToken {
    public final static String TABLE_NAME = "reset_tokens_table";

    @Id
    @EqualsAndHashCode.Include
    @Type(type = "uuid-char")
    private String token;
    private String ownerUsername;
    private Date issuedDate;
    private Date expireDate;
}
