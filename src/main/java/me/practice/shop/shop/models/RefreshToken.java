package me.practice.shop.shop.models;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = RefreshToken.TABLE_NAME, indexes = @Index(name = "refresh_owner_idx", columnList = "username"))
public class RefreshToken {
    public static final String TABLE_NAME = "refresh_tokens_table";

    @Id
    @EqualsAndHashCode.Include
    @Type(type = "uuid-char")
    private UUID value;
    @Column(nullable = false)
    private String username;
    private Date issuedDate;
    @Column(nullable = false)
    private Date expireDate;
}
