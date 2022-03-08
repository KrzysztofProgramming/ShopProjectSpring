package me.practice.shop.shop.database.users;

import me.practice.shop.shop.models.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolesRepository extends MongoRepository<Role, String> {

    @Query("{strength: ?0}")
    Optional<Role> findByStrength(double strength);

    @Query("{name: ?0}")
    Optional<Role> findByName(String name);
}
