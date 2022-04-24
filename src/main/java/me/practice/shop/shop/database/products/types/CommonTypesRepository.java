package me.practice.shop.shop.database.products.types;

import me.practice.shop.shop.models.CommonType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface CommonTypesRepository extends JpaRepository<CommonType, Long> {

    @Query(value = "SELECT t FROM #{#entityName} t WHERE t.name IN ?1")
    Collection<CommonType> getAllByNames(Collection<String> names);

    @Query(value = "SELECT t FROM #{#entityName} t WHERE t.name = ?1")
    Optional<CommonType> findByName(String name);

    @Query(value = "DELETE t FROM #{#entityName} t WHERE t.name = ?1")
    void deleteByName(String name);
}

