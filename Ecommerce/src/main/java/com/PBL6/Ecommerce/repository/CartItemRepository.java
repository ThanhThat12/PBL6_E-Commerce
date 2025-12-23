package com.PBL6.Ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.PBL6.Ecommerce.domain.entity.cart.Cart;
import com.PBL6.Ecommerce.domain.entity.cart.CartItem;
import com.PBL6.Ecommerce.domain.entity.product.ProductVariant;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    // ✅ Tìm tất cả items trong 1 cart
    List<CartItem> findByCartId(Long cartId);
    
    // ✅ Tìm CartItem theo Cart và Product objects
    Optional<CartItem> findByCartAndProductVariant(Cart cart, ProductVariant productVariant);

    // ✅ Tìm CartItem theo cartId và productVariantId
    Optional<CartItem> findByCartIdAndProductVariantId(Long cartId, Long productVariantId);
    
    // ✅ Đếm số CartItem theo productId
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.productVariant.product.id = :productId")
    long countByProductVariant_ProductId(@Param("productId") Long productId);
    
    // ✅ Xóa tất cả items trong 1 cart
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") Long cartId);
    
    // ✅ Xóa các items theo cartId và danh sách variantId
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.productVariant.id IN :variantIds")
    void deleteByCartIdAndVariantIdIn(@Param("cartId") Long cartId, @Param("variantIds") List<Long> variantIds);
    
    // ✅ Xóa tất cả cart items theo productId (khi admin set product inactive)
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.productVariant.product.id = :productId")
    int deleteByProductVariant_ProductId(@Param("productId") Long productId);
    
}
