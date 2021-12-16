package me.practice.shop.shop.database.users;

import me.practice.shop.shop.models.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolesRepository extends MongoRepository<Role, String> {
}
