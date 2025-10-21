package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.Cart;
import com.PBL6.Ecommerce.domain.CartItem;
import com.PBL6.Ecommerce.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    // ✅ Tìm tất cả items trong 1 cart
    List<CartItem> findByCartId(Long cartId);
    
    // ✅ Tìm CartItem theo Cart và Product objects
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    
    // ✅ Tìm CartItem theo cartId và productId
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
    
    // ✅ Xóa tất cả items trong 1 cart
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") Long cartId);
}
