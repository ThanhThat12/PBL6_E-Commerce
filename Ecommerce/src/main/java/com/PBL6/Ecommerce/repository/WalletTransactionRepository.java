package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.entity.order.Order;
import com.PBL6.Ecommerce.domain.entity.payment.Wallet;
import com.PBL6.Ecommerce.domain.entity.payment.WalletTransaction;
import com.PBL6.Ecommerce.domain.entity.payment.WalletTransaction.TransactionType;
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
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    
    // Tìm theo Wallet
    List<WalletTransaction> findByWallet(Wallet wallet);
    
    // Tìm theo Wallet ID
    List<WalletTransaction> findByWalletId(Long walletId);
    
    // Tìm theo Wallet ID và sắp xếp theo thời gian giảm dần
    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);
    
    // Tìm theo Type
    List<WalletTransaction> findByType(TransactionType type);
    
    // Tìm theo Wallet và Type
    List<WalletTransaction> findByWalletAndType(Wallet wallet, TransactionType type);
    
    // Tìm theo Related Order
    List<WalletTransaction> findByRelatedOrder(Order order);
    
    // Tìm theo Related Order ID
    List<WalletTransaction> findByRelatedOrderId(Long orderId);
    
    // Tìm transaction trong khoảng thời gian
    @Query("SELECT wt FROM WalletTransaction wt " +
           "WHERE wt.wallet.id = :walletId " +
           "AND wt.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY wt.createdAt DESC")
    List<WalletTransaction> findTransactionsBetweenDates(
            @Param("walletId") Long walletId,
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    // Tính tổng tiền nạp vào ví
    @Query("SELECT COALESCE(SUM(wt.amount), 0) FROM WalletTransaction wt " +
           "WHERE wt.wallet.id = :walletId AND wt.type = 'DEPOSIT'")
    BigDecimal calculateTotalDeposit(@Param("walletId") Long walletId);
    
    // Tính tổng tiền rút ra
    @Query("SELECT COALESCE(SUM(wt.amount), 0) FROM WalletTransaction wt " +
           "WHERE wt.wallet.id = :walletId AND wt.type = 'WITHDRAWAL'")
    BigDecimal calculateTotalWithdrawal(@Param("walletId") Long walletId);
    
    // Tính tổng tiền thanh toán
    @Query("SELECT COALESCE(SUM(wt.amount), 0) FROM WalletTransaction wt " +
           "WHERE wt.wallet.id = :walletId AND wt.type = 'ORDER_PAYMENT'")
    BigDecimal calculateTotalPayment(@Param("walletId") Long walletId);
    
    // Đếm số transaction theo type
    @Query("SELECT COUNT(wt) FROM WalletTransaction wt " +
           "WHERE wt.wallet.id = :walletId AND wt.type = :type")
    Long countByWalletIdAndType(@Param("walletId") Long walletId, @Param("type") TransactionType type);
    
    // Lấy transaction gần nhất của ví
    Optional<WalletTransaction> findFirstByWalletIdOrderByCreatedAtDesc(Long walletId);
    
    // Tìm transaction theo User (qua Wallet)
    @Query("SELECT wt FROM WalletTransaction wt " +
           "WHERE wt.wallet.user.id = :userId " +
           "ORDER BY wt.createdAt DESC")
    List<WalletTransaction> findByUserId(@Param("userId") Long userId);
    
    // Xóa transaction liên quan đến order
    @Modifying
    @Query("DELETE FROM WalletTransaction wt WHERE wt.relatedOrder.id = :orderId")
    void deleteByRelatedOrderId(@Param("orderId") Long orderId);
    
    // ADMIN Check if order has PAYMENT_TO_SELLER transaction
    @Query("SELECT COUNT(wt) > 0 FROM WalletTransaction wt " +
           "WHERE wt.relatedOrder.id = :orderId " +
           "AND wt.type = 'PAYMENT_TO_SELLER'")
    boolean existsPaymentToSellerForOrder(@Param("orderId") Long orderId);
    
    // ===== ADMIN WALLET TRANSACTION APIs =====
    
    // Get all transactions by wallet ID with pagination (no date filter)
    @Query("SELECT wt FROM WalletTransaction wt " +
           "WHERE wt.wallet.id = :walletId " +
           "ORDER BY wt.id DESC")
    org.springframework.data.domain.Page<WalletTransaction> findByWalletId(
        @Param("walletId") Long walletId,
        org.springframework.data.domain.Pageable pageable
    );
    
    // Get transactions by wallet ID and type with pagination (no date filter)
    @Query("SELECT wt FROM WalletTransaction wt " +
           "WHERE wt.wallet.id = :walletId " +
           "AND wt.type = :type " +
           "ORDER BY wt.id DESC")
    org.springframework.data.domain.Page<WalletTransaction> findByWalletIdAndType(
        @Param("walletId") Long walletId,
        @Param("type") TransactionType type,
        org.springframework.data.domain.Pageable pageable
    );
    
    // Filter by wallet + date range with pagination (DEPRECATED)
    @Query("SELECT wt FROM WalletTransaction wt " +
           "WHERE wt.wallet.id = :walletId " +
           "AND wt.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY wt.createdAt DESC")
    org.springframework.data.domain.Page<WalletTransaction> findByWalletIdAndCreatedAtBetween(
        @Param("walletId") Long walletId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        org.springframework.data.domain.Pageable pageable
    );
    
    // Filter by wallet + type + date range with pagination (DEPRECATED)
    @Query("SELECT wt FROM WalletTransaction wt " +
           "WHERE wt.wallet.id = :walletId " +
           "AND wt.type = :type " +
           "AND wt.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY wt.createdAt DESC")
    org.springframework.data.domain.Page<WalletTransaction> findByWalletIdAndTypeAndCreatedAtBetween(
        @Param("walletId") Long walletId,
        @Param("type") TransactionType type,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        org.springframework.data.domain.Pageable pageable
    );
    
    // Search transactions by multiple criteria (using native query for better string matching)
    @Query(value = "SELECT * FROM wallet_transactions wt " +
           "WHERE wt.wallet_id = :walletId " +
           "AND (" +
           "  CAST(wt.id AS CHAR) LIKE CONCAT('%', :keyword, '%') " +
           "  OR LOWER(wt.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "  OR CAST(wt.related_order_id AS CHAR) LIKE CONCAT('%', :keyword, '%') " +
           "  OR DATE_FORMAT(wt.created_at, '%Y-%m-%d') LIKE CONCAT('%', :keyword, '%') " +
           ") " +
           "ORDER BY wt.id DESC",
           nativeQuery = true)
    org.springframework.data.domain.Page<WalletTransaction> searchTransactions(
        @Param("walletId") Long walletId,
        @Param("keyword") String keyword,
        org.springframework.data.domain.Pageable pageable
    );
}
