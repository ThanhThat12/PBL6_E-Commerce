package com.PBL6.Ecommerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.PBL6.Ecommerce.domain.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
    
    // Đếm số OrderItem theo productId
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.variant.product.id = :productId")
    long countByProductVariant_ProductId(@Param("productId") Long productId);
    
    /**
     * Lấy top 5 sản phẩm bán chạy nhất theo shop
     * Chỉ tính đơn hàng COMPLETED
     */
    @Query("SELECT new com.PBL6.Ecommerce.domain.dto.TopProductDTO(" +
           "p.id, p.name, p.mainImage, SUM(oi.quantity), COUNT(DISTINCT o.id)) " +
           "FROM OrderItem oi " +
           "JOIN oi.order o " +
           "JOIN oi.variant v " +
           "JOIN v.product p " +
           "WHERE o.shop.id = :shopId AND o.status = 'COMPLETED' " +
           "GROUP BY p.id, p.name, p.mainImage " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<com.PBL6.Ecommerce.domain.dto.TopProductDTO> findTopSellingProductsByShop(
        @Param("shopId") Long shopId, 
        org.springframework.data.domain.Pageable pageable);
}