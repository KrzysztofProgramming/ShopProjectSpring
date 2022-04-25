package me.practice.shop.shop.database.files;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DatabaseImage {
//    @Id
    private Long id;
    private String mediaType;
//    @Lob
    private byte[] image;
}
