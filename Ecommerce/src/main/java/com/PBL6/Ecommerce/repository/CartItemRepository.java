package com.PBL6.Ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.PBL6.Ecommerce.domain.Cart;
import com.PBL6.Ecommerce.domain.CartItem;
import com.PBL6.Ecommerce.domain.ProductVariant;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    // Tìm tất cả items trong 1 cart
    List<CartItem> findByCartId(Long cartId);
    
    // Tìm CartItem theo Cart và ProductVariant
    Optional<CartItem> findByCartAndProductVariant(Cart cart, ProductVariant productVariant);
    
    // Tìm CartItem theo cartId và productVariantId
    Optional<CartItem> findByCartIdAndProductVariantId(Long cartId, Long productVariantId);
    
    // Xóa tất cả items trong 1 cart
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") Long cartId);
}
