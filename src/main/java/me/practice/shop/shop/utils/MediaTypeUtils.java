package me.practice.shop.shop.utils;

import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


public class MediaTypeUtils {
    private MediaTypeUtils(){}

    private final static Tika tika = new Tika();

//    @Getter
//    private final static List<String> allowedImages = Collections.unmodifiableList(
//            List.of("image/jpeg", "image/pjpeg", "image/png", "image/bmp"));

    public static boolean isImageOK(MultipartFile file) throws IOException {
        return tika.detect(file.getBytes()).startsWith("image/");
    }

    public static String detectFileType(byte[] file){
        return tika.detect(file);
    }
}
