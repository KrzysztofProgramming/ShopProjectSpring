package me.practice.shop.shop.utils;

import lombok.Getter;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


public class MediaTypeUtils {
    private MediaTypeUtils(){}

    private final static Tika tika = new Tika();

    @Getter
    private final static List<String> allowedImages = Collections.unmodifiableList(
            List.of("image/jpeg", "image/png", "image/bmp"));

    public static boolean isImageOK(MultipartFile file) throws IOException {
        return allowedImages.contains(tika.detect(file.getBytes()));
    }

    public static boolean isImageTypeOK(String type){
        return allowedImages.contains(type);
    }

    public static String detectFileType(byte[] file){
        return tika.detect(file);
    }
}
