package me.practice.shop.shop.database.orders;

import me.practice.shop.shop.models.ShopOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersRepository extends MongoRepository<ShopOrder, String>, OrdersSearcher {
    @Query("{ownerUsername: ?0}")
    Page<ShopOrder> findByOwnerUsername(String username, Pageable pageable);
}
