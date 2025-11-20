package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Vouchers;
import com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherDetailDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherListDTO;
import com.PBL6.Ecommerce.exception.NotFoundException;
import com.PBL6.Ecommerce.repository.VouchersRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for Admin Voucher Management
 */
@Service
public class AdminVoucherService {

    private final VouchersRepository vouchersRepository;

    public AdminVoucherService(VouchersRepository vouchersRepository) {
        this.vouchersRepository = vouchersRepository;
    }

    /**
     * Lấy danh sách tất cả vouchers với phân trang
     * @param pageable - Thông tin phân trang
     * @return Page<AdminVoucherListDTO> - Danh sách vouchers với thông tin cơ bản
     */
    public Page<AdminVoucherListDTO> getAllVouchers(Pageable pageable) {
        return vouchersRepository.findAllVouchersForAdmin(pageable);
    }


    /**
     * Xóa voucher theo ID
     * @param id - ID của voucher cần xóa
     * @throws NotFoundException - Nếu voucher không tồn tại
     */
    @Transactional
    public void deleteVoucher(Long id) {
        // Kiểm tra voucher có tồn tại không
        Vouchers voucher = vouchersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Voucher not found with id: " + id));
        
        // Kiểm tra voucher đã được sử dụng chưa
        if (voucher.getUsedCount() > 0) {
            throw new IllegalStateException("Cannot delete voucher that has been used. Used count: " + voucher.getUsedCount());
        }
        
        // Xóa voucher
        vouchersRepository.deleteById(id);
    }

}
