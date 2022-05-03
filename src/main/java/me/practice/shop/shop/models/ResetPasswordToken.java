package me.practice.shop.shop.models;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Builder
@Entity
@Table(name = ResetPasswordToken.TABLE_NAME,
        indexes = @Index(name = "reset_owner_idx", columnList = "ownerUsername"))
public class ResetPasswordToken {
    public final static String TABLE_NAME = "reset_tokens_table";

    @Id
    @EqualsAndHashCode.Include
    @Type(type = "uuid-char")
    private UUID token;
    private String ownerUsername;
    private Date issuedDate;
    @Column(nullable = false)
    private Date expireDate;
}
