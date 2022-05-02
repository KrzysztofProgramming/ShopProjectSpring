package me.practice.shop.shop.database.orders;

import me.practice.shop.shop.models.ShopOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<ShopOrder, Long>, OrdersSearcher {

    @Query(value = "SELECT o FROM #{#entityName} o WHERE o.ownerUsername = ?1")
    Page<ShopOrder> findByOwnerUsername(String username, Pageable pageable);

    @Query(value = "SELECT o FROM #{#entityName} o WHERE o.id = ?2 AND o.ownerUsername = ?1")
    Optional<ShopOrder> findUserOrderById(String username, Long id);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE #{#entityName} o SET o.status = ?2 WHERE o.id = ?1")
    int changeStatus(Long id, Integer newStatus);

    @Query(value = "SELECT COUNT(o) FROM #{#entityName} o WHERE o.email=?1 AND o.status=?2")
    int countOrders(String email, Integer status);
}
