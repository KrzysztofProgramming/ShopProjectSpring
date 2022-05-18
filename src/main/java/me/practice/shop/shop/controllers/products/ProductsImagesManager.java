package me.practice.shop.shop.controllers.products;

import lombok.SneakyThrows;
import me.practice.shop.shop.database.files.DatabaseImage;
import me.practice.shop.shop.database.files.ImageIdentifier;
import me.practice.shop.shop.database.files.ImageSize;
import me.practice.shop.shop.database.files.ImagesRepository;
import me.practice.shop.shop.utils.GzipUtils;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

@Repository
public class ProductsImagesManager {

    public static final int SMALL_IMAGE_SIZE = 350;
    public static final int ICON_SIZE = 150;

    @Autowired
    private ImagesRepository imagesRepository;

    public ProductsImagesManager(){
        this.initDatabases();
    }

    private void initDatabases(){
//        Set<String> currentCollections =  mongoTemplate.getCollectionNames();
//        if(!currentCollections.contains(SMALL_IMAGES_COL_NAME)){
//            this.mongoTemplate.createCollection(SMALL_IMAGES_COL_NAME);
//        }
//        if(!currentCollections.contains(IMAGES_COL_NAME)){
//            this.mongoTemplate.createCollection(IMAGES_COL_NAME);
//        }
//        if(!currentCollections.contains(ICON_COL_NAME)){
//            this.mongoTemplate.createCollection(ICON_COL_NAME);
//        }
    }


    public Optional<DatabaseImage> getIcon(Long id){
        return this.imagesRepository.findById(new ImageIdentifier(id, ImageSize.ICON))
                .map(this::decompressFunction).or(()->this.getSmallImage(id));
    }

    public Optional<DatabaseImage> getSmallImage(Long id){
        return this.imagesRepository.findById(new ImageIdentifier(id, ImageSize.SMALL))
                .map(this::decompressFunction).or(()->this.getOriginalImage(id));
    }

    public Optional<DatabaseImage> getOriginalImage(Long id){
        return this.imagesRepository.findById(new ImageIdentifier(id, ImageSize.ORIGINAL))
                .map(this::decompressFunction);
    }

    private DatabaseImage decompressFunction(DatabaseImage image){
        try {
            image.setImage(GzipUtils.decompress(image.getImage()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }


    @SneakyThrows
    private void saveAndScaleSmallImage(DatabaseImage image, BufferedImage buffedOriginal, TiffOutputSet exifSet){
        if (buffedOriginal.getHeight() > SMALL_IMAGE_SIZE || buffedOriginal.getWidth() > SMALL_IMAGE_SIZE) {
            BufferedImage buffImg = Scalr.resize(buffedOriginal, SMALL_IMAGE_SIZE);
            ByteArrayOutputStream imgOutput = new ByteArrayOutputStream();
            ImageIO.write(buffImg, image.getMediaType().substring(image.getMediaType().indexOf("/") + 1), imgOutput);

            if (exifSet != null) {
                byte[] resizedImage = imgOutput.toByteArray();
                imgOutput.reset();
                new ExifRewriter().updateExifMetadataLossless(resizedImage, imgOutput, exifSet);
            }

            DatabaseImage smallImage = new DatabaseImage(new ImageIdentifier(image.getId().getOwnerId(),
                    ImageSize.SMALL), image.getMediaType(),
                    GzipUtils.compress(imgOutput.toByteArray()));
            this.imagesRepository.save(smallImage);
        }
    }

    @SneakyThrows
    private void saveAndScaleIcon(DatabaseImage image, BufferedImage buffedOriginal, TiffOutputSet exifSet){
        if (buffedOriginal.getHeight() > ICON_SIZE || buffedOriginal.getWidth() > ICON_SIZE) {
            BufferedImage buffImg = Scalr.resize(buffedOriginal, ICON_SIZE);
            ByteArrayOutputStream imgOutput = new ByteArrayOutputStream();
            ImageIO.write(buffImg, image.getMediaType().substring(image.getMediaType().indexOf("/") + 1), imgOutput);

            if (exifSet != null) {
                byte[] resizedImage = imgOutput.toByteArray();
                imgOutput.reset();
                new ExifRewriter().updateExifMetadataLossless(resizedImage, imgOutput, exifSet);
            }

            DatabaseImage iconImage = new DatabaseImage(new ImageIdentifier(image.getId().getOwnerId(),
                    ImageSize.ICON), image.getMediaType(),
                    GzipUtils.compress(imgOutput.toByteArray()));
            this.imagesRepository.save(iconImage);
        }
    }

    public Optional<DatabaseImage> saveAndScale(DatabaseImage image){

        try {
            TiffOutputSet exifSet = null;
            try {
                JpegImageMetadata metadata = (JpegImageMetadata) Imaging.getMetadata(image.getImage());
                TiffImageMetadata imgMeta = null;
                if (metadata != null) imgMeta = metadata.getExif();
                if (imgMeta != null) exifSet = imgMeta.getOutputSet();
            }
            catch (ClassCastException ignore){}

            BufferedImage buffImg = ImageIO.read(new ByteArrayInputStream(image.getImage()));
            this.saveAndScaleSmallImage(image, buffImg, exifSet);
            this.saveAndScaleIcon(image, buffImg, exifSet);
            image.setImage(GzipUtils.compress(image.getImage()));
            image.getId().setImageSize(ImageSize.ORIGINAL);
            return Optional.of(this.imagesRepository.save(image));
        } catch (IOException | ImageReadException | ImageWriteException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public void deleteProductImages(Long id){
        this.imagesRepository.deleteProductImages(id);
    }

}
