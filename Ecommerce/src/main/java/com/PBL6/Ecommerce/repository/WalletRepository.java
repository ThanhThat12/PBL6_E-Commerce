package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.domain.entity.payment.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    // Tìm ví theo User
    Optional<Wallet> findByUser(User user);
    
    // Tìm ví theo User ID
    Optional<Wallet> findByUserId(Long userId);
    
    // Kiểm tra User đã có ví chưa
    boolean existsByUserId(Long userId);
    
    // Tìm ví có số dư lớn hơn một số tiền
    @Query("SELECT w FROM Wallet w WHERE w.balance > :minBalance")
    List<Wallet> findWalletsWithBalanceGreaterThan(@Param("minBalance") BigDecimal minBalance);
    
    // Tính tổng số dư của tất cả ví
    @Query("SELECT COALESCE(SUM(w.balance), 0) FROM Wallet w")
    BigDecimal calculateTotalBalance();
}
