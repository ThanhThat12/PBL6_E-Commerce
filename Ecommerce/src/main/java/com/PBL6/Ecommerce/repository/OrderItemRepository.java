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
}