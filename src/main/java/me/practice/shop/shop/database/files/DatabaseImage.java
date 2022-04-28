package me.practice.shop.shop.database.files;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.practice.shop.shop.models.BookProduct;

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
    private BookProduct product;

    @Lob
    private byte[] image;

    public DatabaseImage(ImageIdentifier id, String mediaType, byte[] image){
        this(id, mediaType, BookProduct.builder().id(id.getOwnerId()).build(), image);
    }
}
