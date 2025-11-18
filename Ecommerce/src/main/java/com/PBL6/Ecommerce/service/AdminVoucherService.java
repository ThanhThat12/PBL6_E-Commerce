package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Vouchers;
import com.PBL6.Ecommerce.domain.Vouchers.VoucherStatus;
import com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherCreateDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherDetailDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherListDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherStatsDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminUpdateVoucherDTO;
import com.PBL6.Ecommerce.exception.NotFoundException;
import com.PBL6.Ecommerce.repository.VouchersRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminVoucherService {
    
    private final VouchersRepository vouchersRepository;

    public AdminVoucherService(VouchersRepository vouchersRepository) {
        this.vouchersRepository = vouchersRepository;
    }

    /**
     * Lấy danh sách tất cả voucher có phân trang
     * @param page - Số trang (bắt đầu từ 0)
     * @param size - Số lượng voucher mỗi trang
     * @return Page<AdminVoucherListDTO>
     */
    @Transactional(readOnly = true)
    public Page<AdminVoucherListDTO> getAllVouchers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Vouchers> vouchersPage = vouchersRepository.findAllVouchers(pageable);
        
        return vouchersPage.map(voucher -> {
            Long usedCount = vouchersRepository.countUsedByVoucherId(voucher.getId());
            
            return new AdminVoucherListDTO(
                voucher.getId(),
                voucher.getCode(),
                voucher.getDiscountAmount(),
                voucher.getMinOrderValue(),
                voucher.getQuantity(),
                usedCount,
                voucher.getStatus().name()
            );
        });
    }

    /**
     * Xem chi tiết voucher
     * @param id - ID của voucher
     * @return AdminVoucherDetailDTO
     */
    @Transactional(readOnly = true)
    public AdminVoucherDetailDTO getVoucherDetail(Long id) {
        Vouchers voucher = vouchersRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Voucher not found with id: " + id));
        
        String shopName = voucher.getShop() != null ? voucher.getShop().getName() : null;
        
        return new AdminVoucherDetailDTO(
            voucher.getId(),
            voucher.getCode(),
            voucher.getDescription(),
            shopName,
            voucher.getDiscountAmount(),
            voucher.getMinOrderValue(),
            voucher.getQuantity(),
            voucher.getStartDate(),
            voucher.getEndDate(),
            voucher.getStatus().name()
        );
    }

    /**
     * Thêm voucher mới
     * @param dto - AdminVoucherCreateDTO
     * @return Voucher đã tạo
     */
    @Transactional
    public Vouchers createVoucher(AdminVoucherCreateDTO dto) {
        // Validate code không trùng
        if (vouchersRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Voucher code already exists: " + dto.getCode());
        }
        
        // Validate code format (uppercase + numbers only, max 10 chars)
        if (!dto.getCode().matches("^[A-Z0-9]{1,10}$")) {
            throw new IllegalArgumentException("Voucher code must contain only uppercase letters and numbers, max 10 characters");
        }
        
        // Validate discount amount >= 0
        if (dto.getDiscountAmount() < 0) {
            throw new IllegalArgumentException("Discount amount must be at least 0");
        }
        
        // Validate min order value >= 0
        if (dto.getMinOrderValue() != null && dto.getMinOrderValue() < 0) {
            throw new IllegalArgumentException("Minimum order value must be at least 0");
        }
        
        // Validate dates
        if (dto.getEndDate().isBefore(dto.getStartDate()) || dto.getEndDate().isEqual(dto.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        
        Vouchers voucher = new Vouchers();
        voucher.setCode(dto.getCode().toUpperCase()); // Ensure uppercase
        voucher.setDescription(dto.getDescription()); // Có thể null
        voucher.setDiscountAmount(dto.getDiscountAmount());
        voucher.setMinOrderValue(dto.getMinOrderValue() != null ? dto.getMinOrderValue() : 0);
        voucher.setQuantity(dto.getQuantity());
        voucher.setStartDate(dto.getStartDate());
        voucher.setEndDate(dto.getEndDate());
        voucher.setStatus(VoucherStatus.ACTIVE);
        voucher.setShop(null); // Admin voucher không có shop
        
        return vouchersRepository.save(voucher);
    }

    /**
     * Cập nhật voucher
     * @param id - ID của voucher
     * @param dto - AdminUpdateVoucherDTO
     * @return Voucher đã cập nhật
     */
    @Transactional
    public Vouchers updateVoucher(Long id, AdminUpdateVoucherDTO dto) {
        Vouchers voucher = vouchersRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Voucher not found with id: " + id));
        
        // Validate code format
        if (!dto.getCode().matches("^[A-Z0-9]{1,10}$")) {
            throw new IllegalArgumentException("Voucher code must contain only uppercase letters and numbers, max 10 characters");
        }
        
        // Validate code không trùng (trừ chính nó)
        if (!voucher.getCode().equals(dto.getCode()) && vouchersRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Voucher code already exists: " + dto.getCode());
        }
        
        // Validate discount amount >= 0
        if (dto.getDiscountAmount() < 0) {
            throw new IllegalArgumentException("Discount amount must be at least 0");
        }
        
        // Validate min order value >= 0
        if (dto.getMinOrderValue() != null && dto.getMinOrderValue() < 0) {
            throw new IllegalArgumentException("Minimum order value must be at least 0");
        }
        
        // Validate dates
        if (dto.getEndDate().isBefore(dto.getStartDate()) || dto.getEndDate().isEqual(dto.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        
        voucher.setCode(dto.getCode().toUpperCase());
        voucher.setDescription(dto.getDescription()); // Có thể null
        voucher.setDiscountAmount(dto.getDiscountAmount());
        voucher.setMinOrderValue(dto.getMinOrderValue() != null ? dto.getMinOrderValue() : 0);
        voucher.setQuantity(dto.getQuantity());
        voucher.setStartDate(dto.getStartDate());
        voucher.setEndDate(dto.getEndDate());
        
        // Update status nếu có
        if (dto.getStatus() != null) {
            try {
                voucher.setStatus(VoucherStatus.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid voucher status: " + dto.getStatus());
            }
        }
        
        return vouchersRepository.save(voucher);
    }

    /**
     * Xóa voucher
     * @param id - ID của voucher
     */
    @Transactional
    public void deleteVoucher(Long id) {
        if (!vouchersRepository.existsById(id)) {
            throw new NotFoundException("Voucher not found with id: " + id);
        }
        
        vouchersRepository.deleteById(id);
    }
    
    /**
     * Lấy thống kê voucher
     * @return AdminVoucherStatsDTO
     */
    @Transactional(readOnly = true)
    public AdminVoucherStatsDTO getVoucherStats() {
        // Tổng số voucher
        Long totalVouchers = vouchersRepository.count();
        
        // Số voucher active
        Long activeVouchers = vouchersRepository.countByStatus(VoucherStatus.ACTIVE);
        
        // Số voucher expired
        Long expiredVouchers = vouchersRepository.countByStatus(VoucherStatus.EXPIRED);
        
        // Tổng số voucher đã sử dụng
        Long usedVouchers = vouchersRepository.countTotalUsedVouchers();
        
        return new AdminVoucherStatsDTO(totalVouchers, activeVouchers, expiredVouchers, usedVouchers);
    }
}

