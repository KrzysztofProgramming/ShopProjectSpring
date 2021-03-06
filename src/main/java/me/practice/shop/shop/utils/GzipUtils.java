package me.practice.shop.shop.utils;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtils {
    private GzipUtils(){}

    public static byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(data.length);
        GZIPOutputStream gzip = new GZIPOutputStream(byteStream);
        gzip.write(data);
        gzip.finish();
        gzip.close();
        return byteStream.toByteArray();
    }

    public static byte[] decompress(byte[] data) throws IOException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
        GZIPInputStream gzip = new GZIPInputStream(byteStream);
        byte[] decompressed = gzip.readAllBytes();
        gzip.close();
        return decompressed;
    }

}
