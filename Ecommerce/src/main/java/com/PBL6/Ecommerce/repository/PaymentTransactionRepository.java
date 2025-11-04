package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.PaymentTransaction;
import com.PBL6.Ecommerce.domain.PaymentTransaction.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    
    // Tìm theo Order
    List<PaymentTransaction> findByOrder(Order order);
    
    // Tìm theo Order ID
    List<PaymentTransaction> findByOrderId(Long orderId);
    
    // Tìm theo Request ID (unique)
    Optional<PaymentTransaction> findByRequestId(String requestId);
    
    // Tìm theo Trans ID từ MoMo
    Optional<PaymentTransaction> findByTransId(String transId);
    
    // Tìm theo Order ID MoMo
    Optional<PaymentTransaction> findByOrderIdMomo(String orderIdMomo);
    
    // Tìm theo Order và Status
    List<PaymentTransaction> findByOrderAndStatus(Order order, PaymentStatus status);
    
    // Tìm theo Order ID và Status
    List<PaymentTransaction> findByOrderIdAndStatus(Long orderId, PaymentStatus status);
    
    // Tìm transaction thành công của một Order
    Optional<PaymentTransaction> findByOrderIdAndStatus(Long orderId, String status);
    
    // Tìm tất cả transaction theo Status
    List<PaymentTransaction> findByStatus(PaymentStatus status);
    
    // Tìm transaction đang pending quá lâu (để xử lý timeout)
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.status = 'PENDING' " +
           "AND pt.createdAt < :expiredTime")
    List<PaymentTransaction> findExpiredPendingTransactions(@Param("expiredTime") LocalDateTime expiredTime);
    
    // Kiểm tra xem Order đã có transaction thành công chưa
    @Query("SELECT COUNT(pt) > 0 FROM PaymentTransaction pt " +
           "WHERE pt.order.id = :orderId AND pt.status = 'SUCCESS'")
    boolean existsSuccessfulTransactionForOrder(@Param("orderId") Long orderId);
    
    // Đếm số transaction theo status
    @Query("SELECT COUNT(pt) FROM PaymentTransaction pt WHERE pt.status = :status")
    Long countByStatus(@Param("status") PaymentStatus status);
    
    // Lấy transaction gần nhất của một Order
    Optional<PaymentTransaction> findFirstByOrderIdOrderByCreatedAtDesc(Long orderId);
    
    // Tìm transaction trong khoảng thời gian
    @Query("SELECT pt FROM PaymentTransaction pt " +
           "WHERE pt.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY pt.createdAt DESC")
    List<PaymentTransaction> findTransactionsBetweenDates(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    // Tìm transaction theo User (qua Order)
    @Query("SELECT pt FROM PaymentTransaction pt " +
           "WHERE pt.order.user.id = :userId " +
           "ORDER BY pt.createdAt DESC")
    List<PaymentTransaction> findByUserId(@Param("userId") Long userId);
    
    // Tìm transaction theo Shop (qua Order)
    @Query("SELECT pt FROM PaymentTransaction pt " +
           "WHERE pt.order.shop.id = :shopId " +
           "ORDER BY pt.createdAt DESC")
    List<PaymentTransaction> findByShopId(@Param("shopId") Long shopId);
    
    // Tính tổng số tiền đã thanh toán thành công
    @Query("SELECT COALESCE(SUM(pt.amount), 0) FROM PaymentTransaction pt " +
           "WHERE pt.status = 'SUCCESS'")
    Long calculateTotalSuccessfulAmount();
    
    // Tính tổng số tiền theo Shop
    @Query("SELECT COALESCE(SUM(pt.amount), 0) FROM PaymentTransaction pt " +
           "WHERE pt.order.shop.id = :shopId AND pt.status = 'SUCCESS'")
    Long calculateTotalSuccessfulAmountByShop(@Param("shopId") Long shopId);
}
