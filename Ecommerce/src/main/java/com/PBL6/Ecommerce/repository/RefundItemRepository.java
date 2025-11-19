package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.RefundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundItemRepository extends JpaRepository<RefundItem, Long> {
    
    /**
     * Find all refund items for a specific refund
     */
    List<RefundItem> findByRefundId(Long refundId);
    
    /**
     * Find all refund items for a specific order item
     */
    List<RefundItem> findByOrderItemId(Long orderItemId);
}
