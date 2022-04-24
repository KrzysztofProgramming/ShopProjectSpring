package me.practice.shop.shop.database.shoppingCarts;

import me.practice.shop.shop.models.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ShoppingCartsRepository extends JpaRepository<ShoppingCart, String> {
    List<ShoppingCart> deleteByExpireDateLessThan(Date date);
}
