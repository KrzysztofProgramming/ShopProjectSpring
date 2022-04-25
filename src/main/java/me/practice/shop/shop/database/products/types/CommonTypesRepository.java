package me.practice.shop.shop.database.products.types;

import me.practice.shop.shop.models.CommonType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface CommonTypesRepository extends JpaRepository<CommonType, Long> {

    @Query(value = "SELECT t FROM #{#entityName} t WHERE t.name IN ?1")
    Collection<CommonType> getAllByNames(Collection<String> names);

    @Query(value = "SELECT t FROM #{#entityName} t WHERE t.name = ?1")
    Optional<CommonType> findByName(String name);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = "DELETE FROM #{#entityName} t WHERE t.name = ?1")
    void deleteByName(String name);

    @Query(value = "SELECT DISTINCT t.name FROM #{#entityName} t")
    Collection<String> getTypesNames();

    @Query(value = "SELECT DISTINCT t.id FROM #{#entityName} t")
    Collection<Long> getTypesIds();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE #{#entityName} t SET t.name = ?2 WHERE t.name = ?1")
    long updateName(String oldName, String newName);

}

