package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.constant.RefundStatus;
import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.Refund;
import com.PBL6.Ecommerce.domain.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    
    // Tìm theo Order
    List<Refund> findByOrder(Order order);
    
    // Tìm theo Order ID
    List<Refund> findByOrderId(Long orderId);
    
    // Tìm theo Status
    List<Refund> findByStatus(RefundStatus status);
    
    // Tìm theo Order và Status
    List<Refund> findByOrderAndStatus(Order order, RefundStatus status);
    
    // Tìm theo Order ID và Status
    List<Refund> findByOrderIdAndStatus(Long orderId, RefundStatus status);
    
    // Tìm theo Wallet Transaction
    Optional<Refund> findByTransaction(WalletTransaction transaction);
    
    // Kiểm tra Order đã có refund hoàn thành chưa
    @Query("SELECT COUNT(r) > 0 FROM Refund r " +
           "WHERE r.order.id = :orderId AND r.status = 'COMPLETED'")
    boolean existsCompletedRefundForOrder(@Param("orderId") Long orderId);
    
    // Tính tổng số tiền đã refund cho một Order
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Refund r " +
           "WHERE r.order.id = :orderId AND r.status = 'COMPLETED'")
    BigDecimal calculateTotalRefundedAmountForOrder(@Param("orderId") Long orderId);
    
    // Đếm số refund theo status
    @Query("SELECT COUNT(r) FROM Refund r WHERE r.status = :status")
    Long countByStatus(@Param("status") RefundStatus status);
    
    // Lấy refund gần nhất của một Order
    Optional<Refund> findFirstByOrderIdOrderByCreatedAtDesc(Long orderId);
    
    // Tìm refund trong khoảng thời gian
    @Query("SELECT r FROM Refund r " +
           "WHERE r.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY r.createdAt DESC")
    List<Refund> findRefundsBetweenDates(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    // Tìm refund theo User (qua Order)
    @Query("SELECT r FROM Refund r " +
           "WHERE r.order.user.id = :userId " +
           "ORDER BY r.createdAt DESC")
    List<Refund> findByUserId(@Param("userId") Long userId);
    
    // Tìm refund theo Shop (qua Order)
    @Query("SELECT r FROM Refund r " +
           "WHERE r.order.shop.id = :shopId " +
           "ORDER BY r.createdAt DESC")
    List<Refund> findByShopId(@Param("shopId") Long shopId);
    
    // Tính tổng số tiền đã refund thành công
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Refund r " +
           "WHERE r.status = 'COMPLETED'")
    BigDecimal calculateTotalCompletedRefundAmount();
    
    // Xóa refund liên quan đến order
    @Modifying
    @Query("DELETE FROM Refund r WHERE r.order.id = :orderId")
    void deleteByOrderId(@Param("orderId") Long orderId);
}
