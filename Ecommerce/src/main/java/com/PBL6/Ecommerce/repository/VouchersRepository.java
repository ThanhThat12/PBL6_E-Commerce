package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.Vouchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    boolean existsByCode(String code);
    
    /**
     * Tìm voucher theo code
     */
    Optional<Vouchers> findByCode(String code);
    
    /**
     * Lấy tất cả vouchers với phân trang
     * Sắp xếp theo ID tăng dần (cũ nhất trước)
     */
    @Query("SELECT v FROM Vouchers v ORDER BY v.id ASC")
    Page<Vouchers> findAllVouchers(Pageable pageable);
    
    /**
     * Đếm số lượng voucher đã sử dụng theo voucher ID
     */
    @Query("SELECT COUNT(uv) FROM User_Vouchers uv WHERE uv.voucher.id = :voucherId")
    Long countUsedByVoucherId(@Param("voucherId") Long voucherId);
    
    /**
     * Đếm tổng số voucher đã sử dụng (tất cả vouchers)
     */
    @Query("SELECT COUNT(uv) FROM User_Vouchers uv")
    Long countTotalUsedVouchers();
    
    /**
     * Đếm số lượng voucher theo status
     */
    long countByStatus(Vouchers.VoucherStatus status);
}

