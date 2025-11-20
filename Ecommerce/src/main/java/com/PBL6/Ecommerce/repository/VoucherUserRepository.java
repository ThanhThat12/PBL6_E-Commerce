package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.VoucherUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherUserRepository extends JpaRepository<VoucherUser, Long> {
    
    // Lấy tất cả user IDs của voucher
    @Query("SELECT vu.user.id FROM VoucherUser vu WHERE vu.voucher.id = :voucherId")
    List<Long> findUserIdsByVoucherId(@Param("voucherId") Long voucherId);
    
    // Xóa tất cả user của voucher
    void deleteByVoucherId(Long voucherId);
    
    // Kiểm tra user có được áp dụng voucher không
    boolean existsByVoucherIdAndUserId(Long voucherId, Long userId);
}
