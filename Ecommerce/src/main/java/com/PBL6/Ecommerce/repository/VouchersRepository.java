package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.entity.voucher.Vouchers;
import com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherDetailDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
     * Sorting được điều khiển bởi Pageable từ Controller
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
           "FROM Vouchers v")
    Page<com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherListDTO> findAllVouchersForAdmin(Pageable pageable);
    
    /**
     * ADMIN - Lấy danh sách vouchers theo status với phân trang
     * Sorting được điều khiển bởi Pageable từ Controller
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
           "WHERE v.status = :status")
    Page<com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherListDTO> findVouchersByStatus(@Param("status") Vouchers.Status status, Pageable pageable);


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
     * ADMIN - Search vouchers by multiple criteria
     * Search by: code, discount type, discount value, date (DD/MM/YYYY)
     */
    @Query(value = "SELECT * FROM vouchers v " +
           "WHERE (" +
           "  CAST(v.id AS CHAR) LIKE CONCAT('%', :keyword, '%') " +
           "  OR v.code LIKE CONCAT('%', :keyword, '%') " +
           "  OR CAST(v.discount_type AS CHAR) LIKE CONCAT('%', :keyword, '%') " +
           "  OR CAST(v.discount_value AS CHAR) LIKE CONCAT('%', :keyword, '%') " +
           "  OR DATE_FORMAT(v.start_date, '%d/%m/%Y') LIKE CONCAT('%', :keyword, '%') " +
           "  OR DATE_FORMAT(v.end_date, '%d/%m/%Y') LIKE CONCAT('%', :keyword, '%') " +
           ")",
           nativeQuery = true)
    Page<Vouchers> searchVouchers(@Param("keyword") String keyword, Pageable pageable);
}
