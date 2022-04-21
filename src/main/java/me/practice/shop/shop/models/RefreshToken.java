package me.practice.shop.shop.models;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = RefreshToken.TABLE_NAME, indexes = @Index(name = "index_username", columnList = "username"))
public class RefreshToken {
    public static final String TABLE_NAME = "refresh_tokens_table";

    @Id
    @EqualsAndHashCode.Include
    private String value;
    private String username;
    private Date issuedDate;
    private Date expireDate;
}
