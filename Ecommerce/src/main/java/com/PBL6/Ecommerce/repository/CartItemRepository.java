package com.PBL6.Ecommerce.repository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.PBL6.Ecommerce.domain.Cart;
import com.PBL6.Ecommerce.domain.CartItem;
import com.PBL6.Ecommerce.domain.Product;
import java.util.Optional;
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
