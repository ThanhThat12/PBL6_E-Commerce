package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.Wallet;
import com.PBL6.Ecommerce.domain.WalletTransaction;
import com.PBL6.Ecommerce.domain.WalletTransaction.TransactionType;
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
}
