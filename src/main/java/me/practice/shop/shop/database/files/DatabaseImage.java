package me.practice.shop.shop.database.files;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.practice.shop.shop.models.BookProduct;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = DatabaseImage.TABLE_NAME)
public class DatabaseImage {
    public static final String TABLE_NAME = "images_table";

    @EmbeddedId
    private ImageIdentifier id;
    private String mediaType;

    @MapsId("ownerId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private BookProduct product;

    @Lob
    private byte[] image;

    public DatabaseImage(ImageIdentifier id, String mediaType, byte[] image){
        this(id, mediaType, BookProduct.builder().id(id.getOwnerId()).build(), image);
    }
}
