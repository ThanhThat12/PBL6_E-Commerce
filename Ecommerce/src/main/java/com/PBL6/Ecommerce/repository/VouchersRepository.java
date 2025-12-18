package com.PBL6.Ecommerce.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.PBL6.Ecommerce.domain.entity.voucher.Vouchers;

@Repository
public interface VouchersRepository extends JpaRepository<Vouchers, Long> {
    
    /**
     * Xóa tất cả vouchers của một shop
     */
    @Modifying
    @Query("DELETE FROM Vouchers v WHERE v.shop.id = :shopId")
    void deleteByShopId(@Param("shopId") Long shopId);
    
    /**
     * Kiểm tra voucher code đã tồn tại chưa
     */
    // boolean existsByCode(String code);
    
    /**
     * Tìm voucher theo code
     */
    // Optional<Vouchers> findByCode(String code);
    
    /** ADMIN
     * Lấy danh sách vouchers với thông tin cơ bản 
     * Sắp xếp theo ID tăng dần
     */
    @Query("SELECT new com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherListDTO(" +
           "v.id, " +
           "v.code, " +
           "CAST(v.discountType AS string), " +
           "v.discountValue, " +
           "v.minOrderValue, " +
           "v.usageLimit, " +
           "v.usedCount, " +
           "CAST(v.status AS string)) " +
           "FROM Vouchers v " +
           "ORDER BY v.id ASC")
    Page<com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherListDTO> findAllVouchersForAdmin(Pageable pageable);
    
    
    


    // Tìm voucher theo code
    Optional<Vouchers> findByCode(String code);
    
    // Lấy tất cả voucher của shop
    List<Vouchers> findByShopId(Long shopId);
    
    // Lấy voucher đang active của shop
        @Query("SELECT v FROM Vouchers v WHERE v.shop.id = :shopId AND v.status = 'ACTIVE' AND v.endDate > :currentDate")
        List<Vouchers> findActiveVouchersByShop(@Param("shopId") Long shopId, @Param("currentDate") LocalDateTime currentDate);
    
    // Kiểm tra voucher code đã tồn tại
    boolean existsByCode(String code);

    @Query("SELECT v FROM Vouchers v WHERE v.status = 'ACTIVE' AND v.startDate <= :now AND v.endDate >= :now")
    List<Vouchers> findActiveVouchers(@Param("now") LocalDateTime now);

    /**
     * ADMIN Đếm tổng số vouchers
     */
    @Query("SELECT COUNT(v) FROM Vouchers v")
    Long countTotalVouchers();

    /**
     * ADMIN Đếm số vouchers đang active
     */
    @Query("SELECT COUNT(v) FROM Vouchers v WHERE v.status = 'ACTIVE'")
    Long countActiveVouchers();

    /**
     * ADMIN Tính tổng số lượt sử dụng của tất cả vouchers
     */
    @Query("SELECT COALESCE(SUM(v.usedCount), 0) FROM Vouchers v")
    Long sumUsedVouchers();

    /**
     * PUBLIC Lấy platform vouchers (shop IS NULL) đang active và trong thời gian hiệu lực
     */
    @Query("SELECT v FROM Vouchers v WHERE v.shop IS NULL " +
           "AND v.status = 'ACTIVE' " +
           "AND v.startDate <= :now " +
           "AND v.endDate >= :now " +
           "ORDER BY v.createdAt DESC")
    Page<Vouchers> findActivePlatformVouchers(@Param("now") LocalDateTime now, Pageable pageable);
}
