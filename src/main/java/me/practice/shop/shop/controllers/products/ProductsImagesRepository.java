package me.practice.shop.shop.controllers.products;

import me.practice.shop.shop.database.files.DatabaseImage;
import me.practice.shop.shop.utils.GzipUtils;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.bson.types.Binary;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Repository
public class ProductsImagesRepository {

    private static final String IMAGES_COL_NAME = "products_images";
    private static final String SMALL_IMAGES_COL_NAME = "products_images_small";
    public static final int SMALL_IMAGE_SIZE = 300;

    private final MongoTemplate mongoTemplate;

    @Autowired
    public ProductsImagesRepository(MongoTemplate template){
        this.mongoTemplate = template;
        this.initDatabases();
    }

    private void initDatabases(){
        Set<String> currentCollections =  mongoTemplate.getCollectionNames();
        if(!currentCollections.contains(SMALL_IMAGES_COL_NAME)){
            this.mongoTemplate.createCollection(SMALL_IMAGES_COL_NAME);
        }
        if(!currentCollections.contains(IMAGES_COL_NAME)){
            this.mongoTemplate.createCollection(IMAGES_COL_NAME);
        }
    }

    private void saveOriginalImage(DatabaseImage image){
        this.mongoTemplate.save(image, IMAGES_COL_NAME);
    }

    private void saveSmallImage(DatabaseImage image){
        this.mongoTemplate.save(image, SMALL_IMAGES_COL_NAME);
    }

    public Optional<DatabaseImage> getSmallImage(String id){
        return Optional.ofNullable(this.mongoTemplate.findById(id, DatabaseImage.class, SMALL_IMAGES_COL_NAME))
                .map( image -> {
                    try {
                        image.setImage(GzipUtils.decompress(image.getImage()));
                        return image;
                    } catch (IOException e) {
                        return null;
                    }
                }).or(()->this.getOriginalImage(id));
    }

    public Optional<DatabaseImage> getOriginalImage(String id){
        return Optional.ofNullable(this.mongoTemplate.findById(id, DatabaseImage.class, IMAGES_COL_NAME))
                .map(image->{
                    try {
                        image.setImage(GzipUtils.decompress(image.getImage()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return image;
                });
    }

//    public boolean saveAndScale(DatabaseImage image){
//        return saveAndScale(image, new ByteArrayInputStream(image.getImage().getData()));
//    }

    public boolean saveAndScale(DatabaseImage image){

        try {
            JpegImageMetadata metadata = (JpegImageMetadata) Imaging.getMetadata(image.getImage().getData());
            TiffImageMetadata imgMeta = null;
            if(metadata !=null) imgMeta = metadata.getExif();
            TiffOutputSet exifSet = null;
            if(imgMeta != null) exifSet = imgMeta.getOutputSet();
            System.out.println(exifSet);

            BufferedImage buffImg = ImageIO.read(new ByteArrayInputStream(image.getImage().getData()));
            if(buffImg.getHeight() > SMALL_IMAGE_SIZE || buffImg.getWidth() > SMALL_IMAGE_SIZE){
                buffImg = Scalr.resize(buffImg, SMALL_IMAGE_SIZE);
                ByteArrayOutputStream imgOutput = new ByteArrayOutputStream();
                ImageIO.write(buffImg, image.getMediaType().substring(image.getMediaType().indexOf("/") + 1), imgOutput);

                if(exifSet !=null){
                    byte[] resizedImage = imgOutput.toByteArray();
                    imgOutput.reset();
                    new ExifRewriter().updateExifMetadataLossless(resizedImage, imgOutput, exifSet);
                }

                DatabaseImage smallImage = new DatabaseImage(image.getId(), image.getMediaType(),
                        new Binary(GzipUtils.compress(imgOutput.toByteArray())));
                this.saveSmallImage(smallImage);
            }
            image.setImage(new Binary(GzipUtils.compress(image.getImage().getData())));
            this.saveOriginalImage(image);
        } catch (IOException | ImageReadException | ClassCastException | ImageWriteException e) {
//            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void deleteProductImages(String id){
        this.mongoTemplate.remove(new Query(Criteria.where("_id").is(id)), SMALL_IMAGES_COL_NAME);
        this.mongoTemplate.remove(new Query(Criteria.where("_id").is(id)), IMAGES_COL_NAME);
    }

}