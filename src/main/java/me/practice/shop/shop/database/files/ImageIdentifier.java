package me.practice.shop.shop.database.files;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageIdentifier implements Serializable {

    private Long ownerId;

    @Enumerated(value = EnumType.STRING)
    private ImageSize imageSize;
}
