package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Vouchers;
import com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherCreateDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherUpdateDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherDetailDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherListDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherStatsDTO;
import com.PBL6.Ecommerce.exception.NotFoundException;
import com.PBL6.Ecommerce.repository.VouchersRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
     * Lấy thống kê vouchers
     * @return AdminVoucherStatsDTO - Thống kê về vouchers
     */
    @Transactional(readOnly = true)
    public AdminVoucherStatsDTO getVoucherStats() {
        Long totalVouchers = vouchersRepository.countTotalVouchers();
        Long activeVouchers = vouchersRepository.countActiveVouchers();
        Long usedVouchers = vouchersRepository.sumUsedVouchers();

        return new AdminVoucherStatsDTO(totalVouchers, activeVouchers, usedVouchers);
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

    /**
     * Lấy chi tiết voucher theo ID (simplified version)
     * @param voucherId - ID của voucher
     * @return AdminVoucherDetailDTO - Thông tin chi tiết voucher
     * @throws NotFoundException - Nếu voucher không tồn tại
     */
    @Transactional(readOnly = true)
    public AdminVoucherDetailDTO getVoucherDetail(Long voucherId) {
        // Lấy voucher
        Vouchers voucher = vouchersRepository.findById(voucherId)
                .orElseThrow(() -> new NotFoundException("Voucher not found with id: " + voucherId));

        // Tạo DTO
        AdminVoucherDetailDTO dto = new AdminVoucherDetailDTO();
        dto.setId(voucher.getId());
        dto.setCode(voucher.getCode());
        dto.setDescription(voucher.getDescription());
        dto.setDiscountType(voucher.getDiscountType().name());
        dto.setDiscountValue(voucher.getDiscountValue());
        dto.setMinOrderValue(voucher.getMinOrderValue());
        dto.setMaxDiscountAmount(voucher.getMaxDiscountAmount());
        dto.setUsageLimit(voucher.getUsageLimit());
        dto.setUsedCount(voucher.getUsedCount());
        
        // Tính toán remainingUses
        Integer remainingUses = voucher.getUsageLimit() - voucher.getUsedCount();
        dto.setRemainingUses(Math.max(0, remainingUses));
        
        // Use status from database directly
        dto.setStatus(voucher.getStatus().name());
        dto.setApplicableType(voucher.getApplicableType().name());
        
        dto.setStartDate(voucher.getStartDate());
        dto.setEndDate(voucher.getEndDate());
        dto.setCreatedAt(voucher.getCreatedAt());
        dto.setUpdatedAt(voucher.getCreatedAt()); // Use createdAt as updatedAt since entity doesn't have updatedAt
        
        // Map shop info
        if (voucher.getShop() != null) {
            AdminVoucherDetailDTO.ShopInfo shopInfo = new AdminVoucherDetailDTO.ShopInfo(
                voucher.getShop().getId(),
                voucher.getShop().getName()
            );
            dto.setShop(shopInfo);
        } else {
            // Platform voucher - return special ShopInfo
            AdminVoucherDetailDTO.ShopInfo shopInfo = new AdminVoucherDetailDTO.ShopInfo(
                null,
                "Platform Voucher"
            );
            dto.setShop(shopInfo);
        }
        
        return dto;
    }

    /**
     * Tạo voucher mới (Admin only)
     * Voucher được tạo bởi admin sẽ là platform voucher (shop_id = null)
     * @param createDTO - DTO chứa thông tin voucher cần tạo
     * @return AdminVoucherDetailDTO - Thông tin voucher vừa tạo
     * @throws IllegalArgumentException - Nếu dữ liệu không hợp lệ
     */
    @Transactional
    public AdminVoucherDetailDTO createVoucher(AdminVoucherCreateDTO createDTO) {
        // Validate: Code must be unique
        if (vouchersRepository.existsByCode(createDTO.getCode())) {
            throw new IllegalArgumentException("Voucher code already exists: " + createDTO.getCode());
        }

        // Validate: End date must be after start date
        if (createDTO.getEndDate().isBefore(createDTO.getStartDate()) || 
            createDTO.getEndDate().isEqual(createDTO.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Validate: For PERCENTAGE type, maxDiscountAmount is required
        if (createDTO.getDiscountType() == Vouchers.DiscountType.PERCENTAGE) {
            if (createDTO.getMaxDiscountAmount() == null || createDTO.getMaxDiscountAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Max discount amount is required for percentage discount type");
            }
            // Validate: Percentage value must be between 0 and 100
            if (createDTO.getDiscountValue().compareTo(java.math.BigDecimal.valueOf(100)) > 0) {
                throw new IllegalArgumentException("Percentage discount value cannot exceed 100");
            }
        }

        // Validate: Status can only be ACTIVE or UPCOMING (not EXPIRED)
        if (createDTO.getStatus() == Vouchers.Status.EXPIRED) {
            throw new IllegalArgumentException("Cannot create voucher with EXPIRED status");
        }

        // Create new voucher entity
        Vouchers voucher = new Vouchers();
        voucher.setCode(createDTO.getCode().toUpperCase());
        voucher.setDescription(createDTO.getDescription());
        voucher.setShop(null); // Platform voucher - admin created
        voucher.setDiscountType(createDTO.getDiscountType());
        voucher.setDiscountValue(createDTO.getDiscountValue());
        voucher.setMinOrderValue(createDTO.getMinOrderValue());
        voucher.setMaxDiscountAmount(createDTO.getMaxDiscountAmount());
        voucher.setStartDate(createDTO.getStartDate());
        voucher.setEndDate(createDTO.getEndDate());
        voucher.setUsageLimit(createDTO.getUsageLimit());
        voucher.setUsedCount(0);
        voucher.setApplicableType(createDTO.getApplicableType());
        voucher.setTopBuyersCount(null); // Not used for admin-created vouchers
        voucher.setStatus(createDTO.getStatus());
        voucher.setCreatedAt(LocalDateTime.now());

        // Save voucher
        Vouchers savedVoucher = vouchersRepository.save(voucher);

        // Return detail DTO
        return getVoucherDetail(savedVoucher.getId());
    }

    /**
     * Cập nhật voucher (Admin only)
     * Note: applicableType không thể thay đổi sau khi tạo
     * @param voucherId - ID của voucher cần cập nhật
     * @param updateDTO - DTO chứa thông tin cần cập nhật
     * @return AdminVoucherDetailDTO - Thông tin voucher sau khi cập nhật
     * @throws NotFoundException - Nếu voucher không tồn tại
     * @throws IllegalArgumentException - Nếu dữ liệu không hợp lệ
     */
    @Transactional
    public AdminVoucherDetailDTO updateVoucher(Long voucherId, AdminVoucherUpdateDTO updateDTO) {
        // Tìm voucher cần update
        Vouchers voucher = vouchersRepository.findById(voucherId)
                .orElseThrow(() -> new NotFoundException("Voucher not found with id: " + voucherId));

        // Validate: Nếu code thay đổi, phải unique
        if (!voucher.getCode().equals(updateDTO.getCode().toUpperCase())) {
            if (vouchersRepository.existsByCode(updateDTO.getCode())) {
                throw new IllegalArgumentException("Voucher code already exists: " + updateDTO.getCode());
            }
        }

        // Validate: End date must be after start date
        if (updateDTO.getEndDate().isBefore(updateDTO.getStartDate()) || 
            updateDTO.getEndDate().isEqual(updateDTO.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Validate: For PERCENTAGE type, maxDiscountAmount is required
        if (updateDTO.getDiscountType() == Vouchers.DiscountType.PERCENTAGE) {
            if (updateDTO.getMaxDiscountAmount() == null || updateDTO.getMaxDiscountAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Max discount amount is required for percentage discount type");
            }
            // Validate: Percentage value must be between 0 and 100
            if (updateDTO.getDiscountValue().compareTo(java.math.BigDecimal.valueOf(100)) > 0) {
                throw new IllegalArgumentException("Percentage discount value cannot exceed 100");
            }
        }

        // Validate: Usage limit không được nhỏ hơn số lượt đã dùng
        if (updateDTO.getUsageLimit() < voucher.getUsedCount()) {
            throw new IllegalArgumentException("Usage limit cannot be less than used count (" + voucher.getUsedCount() + ")");
        }

        // Update voucher fields
        voucher.setCode(updateDTO.getCode().toUpperCase());
        voucher.setDescription(updateDTO.getDescription());
        voucher.setDiscountType(updateDTO.getDiscountType());
        voucher.setDiscountValue(updateDTO.getDiscountValue());
        voucher.setMinOrderValue(updateDTO.getMinOrderValue());
        voucher.setMaxDiscountAmount(updateDTO.getMaxDiscountAmount());
        voucher.setStartDate(updateDTO.getStartDate());
        voucher.setEndDate(updateDTO.getEndDate());
        voucher.setUsageLimit(updateDTO.getUsageLimit());
        voucher.setStatus(updateDTO.getStatus());
        // Note: applicableType, shop, usedCount, topBuyersCount are NOT updated

        // Save voucher
        Vouchers updatedVoucher = vouchersRepository.save(voucher);

        // Return detail DTO
        return getVoucherDetail(updatedVoucher.getId());
    }

}
