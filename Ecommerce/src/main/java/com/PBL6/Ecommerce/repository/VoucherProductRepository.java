package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.Product;
import com.PBL6.Ecommerce.domain.VoucherProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherProductRepository extends JpaRepository<VoucherProduct, Long> {
    
    // Lấy tất cả product IDs của voucher
    @Query("SELECT vp.product.id FROM VoucherProduct vp WHERE vp.voucher.id = :voucherId")
    List<Long> findProductIdsByVoucherId(@Param("voucherId") Long voucherId);
    
    // ADMIN Lấy tất cả Product objects của voucher (cho admin detail)
    @Query("SELECT vp.product FROM VoucherProduct vp WHERE vp.voucher.id = :voucherId")
    List<Product> findProductsByVoucherId(@Param("voucherId") Long voucherId);
    
    // Xóa tất cả product của voucher
    void deleteByVoucherId(Long voucherId);
    
    // Kiểm tra voucher có áp dụng cho product không
    @Query("SELECT COUNT(vp) > 0 FROM VoucherProduct vp WHERE vp.voucher.id = :voucherId AND vp.product.id = :productId")
    boolean existsByVoucherIdAndProductId(@Param("voucherId") Long voucherId, @Param("productId") Long productId);
}
