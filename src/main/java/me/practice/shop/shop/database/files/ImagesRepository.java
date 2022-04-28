package me.practice.shop.shop.database.files;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface ImagesRepository extends JpaRepository<DatabaseImage, ImageIdentifier> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = "DELETE FROM #{#entityName} i WHERE i.id.ownerId = ?1")
    void deleteProductImages(Long id);
}
