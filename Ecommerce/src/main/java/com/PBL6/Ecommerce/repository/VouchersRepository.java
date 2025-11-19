package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.Vouchers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VouchersRepository extends JpaRepository<Vouchers, Long> {
    
    // Tìm voucher theo code
    Optional<Vouchers> findByCode(String code);
    
    // Lấy tất cả voucher của shop
    List<Vouchers> findByShopId(Long shopId);
    
    // Lấy voucher đang active của shop
    @Query("SELECT v FROM Vouchers v WHERE v.shop.id = :shopId AND v.isActive = true " +
           "AND v.startDate <= :now AND v.endDate >= :now " +
           "ORDER BY v.createdAt DESC")
    List<Vouchers> findActiveVouchersByShop(@Param("shopId") Long shopId, @Param("now") LocalDateTime now);
    
    // Kiểm tra voucher code đã tồn tại
    boolean existsByCode(String code);

    @Query("SELECT v FROM Vouchers v WHERE v.isActive = true AND v.startDate <= :now AND v.endDate >= :now")
    List<Vouchers> findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        @Param("now") LocalDateTime now, @Param("now") LocalDateTime nowAgain);
}
