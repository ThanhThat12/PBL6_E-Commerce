package com.PBL6.Ecommerce.controller.admin;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherCreateDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherUpdateDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherDetailDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherListDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherStatsDTO;
import com.PBL6.Ecommerce.service.AdminVoucherService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Admin Voucher Management
 * Admin can: view all vouchers with pagination, view voucher details
 */
@Tag(name = "Admin Vouchers", description = "Admin voucher management")
@RestController
@RequestMapping("/api/admin")
public class AdminVoucherController {

    private final AdminVoucherService adminVoucherService;

    public AdminVoucherController(AdminVoucherService adminVoucherService) {
        this.adminVoucherService = adminVoucherService;
    }

    /**
     * API lấy danh sách tất cả vouchers với phân trang (Admin only)
     * GET /api/admin/vouchers?page=0&size=10
     * Sorted by ID descending (newest first)
     * @param page - Trang hiện tại (bắt đầu từ 0)
     * @param size - Số lượng items trên mỗi trang (mặc định 10)
     * @return ResponseDTO<Page<AdminVoucherListDTO>> - Danh sách vouchers với phân trang
     */
    @GetMapping("/vouchers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<AdminVoucherListDTO>>> getAllVouchers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"));
        Page<AdminVoucherListDTO> vouchers = adminVoucherService.getAllVouchers(pageable);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Vouchers retrieved successfully", vouchers));
    }

    /**
     * API lấy danh sách vouchers theo status với phân trang (Admin only)
     * GET /api/admin/vouchers/status/{status}?page=0&size=10
     * Sorted by ID descending (newest first)
     * @param status - Trạng thái voucher (ACTIVE, UPCOMING, EXPIRED)
     * @param page - Trang hiện tại (bắt đầu từ 0)
     * @param size - Số lượng items trên mỗi trang (mặc định 10)
     * @return ResponseDTO<Page<AdminVoucherListDTO>> - Danh sách vouchers theo status
     */
    @GetMapping("/vouchers/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<AdminVoucherListDTO>>> getVouchersByStatus(
            @PathVariable String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        // Convert string to enum
        com.PBL6.Ecommerce.domain.entity.voucher.Vouchers.Status voucherStatus;
        try {
            voucherStatus = com.PBL6.Ecommerce.domain.entity.voucher.Vouchers.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, "Invalid status. Valid values: ACTIVE, UPCOMING, EXPIRED", null, null)
            );
        }
        
        Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"));
        Page<AdminVoucherListDTO> vouchers = adminVoucherService.getVouchersByStatus(voucherStatus, pageable);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Vouchers retrieved successfully", vouchers));
    }

    /**
     * API search vouchers by keyword (Admin only)
     * GET /api/admin/vouchers/search?keyword=CODE123&page=0&size=10
     * Search by: code, discount type, discount value, date (DD/MM/YYYY)
     * @param keyword - Search keyword
     * @param page - Trang hiện tại (bắt đầu từ 0)
     * @param size - Số lượng items trên mỗi trang (mặc định 10)
     * @return ResponseDTO<Page<AdminVoucherListDTO>> - Search results
     */
    @GetMapping("/vouchers/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<AdminVoucherListDTO>>> searchVouchers(
            @RequestParam String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"));
        Page<AdminVoucherListDTO> vouchers = adminVoucherService.searchVouchers(keyword, pageable);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Search completed successfully", vouchers));
    }

    /**
     * API lấy thống kê vouchers (Admin only)
     * GET /api/admin/vouchers/stats
     * @return ResponseDTO<AdminVoucherStatsDTO> - Thống kê vouchers
     */
    @GetMapping("/vouchers/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminVoucherStatsDTO>> getVoucherStats() {
        AdminVoucherStatsDTO stats = adminVoucherService.getVoucherStats();
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Voucher stats retrieved successfully", stats));
    }

    /**
     * API xóa voucher theo ID (Admin only)
     * DELETE /api/admin/voucher/{id}
     * @param id - ID của voucher cần xóa
     * @return ResponseDTO<String> - Thông báo xóa thành công
     */
    @DeleteMapping("/voucher/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<String>> deleteVoucher(@PathVariable Long id) {
        adminVoucherService.deleteVoucher(id);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Voucher deleted successfully", "Voucher with ID " + id + " has been deleted"));
    }

    /**
     * API lấy chi tiết voucher theo ID (Admin only)
     * GET /api/admin/vouchers/{id}
     * @param id - ID của voucher
     * @return ResponseDTO<AdminVoucherDetailDTO> - Thông tin chi tiết voucher
     */
    @GetMapping("/vouchers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminVoucherDetailDTO>> getVoucherDetail(@PathVariable Long id) {
        AdminVoucherDetailDTO voucherDetail = adminVoucherService.getVoucherDetail(id);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Voucher detail retrieved successfully", voucherDetail));
    }

    /**
     * API tạo voucher mới (Admin only)
     * POST /api/admin/vouchers
     * @param createDTO - DTO chứa thông tin voucher cần tạo
     * @return ResponseDTO<AdminVoucherDetailDTO> - Thông tin voucher vừa tạo
     */
    @PostMapping("/vouchers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminVoucherDetailDTO>> createVoucher(
            @Valid @RequestBody AdminVoucherCreateDTO createDTO) {
        AdminVoucherDetailDTO createdVoucher = adminVoucherService.createVoucher(createDTO);
        return ResponseEntity.ok(new ResponseDTO<>(201, null, "Voucher created successfully", createdVoucher));
    }

    /**
     * API cập nhật voucher (Admin only)
     * PUT /api/admin/vouchers/{id}
     * @param id - ID của voucher cần cập nhật
     * @param updateDTO - DTO chứa thông tin cần cập nhật
     * @return ResponseDTO<AdminVoucherDetailDTO> - Thông tin voucher sau khi cập nhật
     */
    @PutMapping("/vouchers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminVoucherDetailDTO>> updateVoucher(
            @PathVariable Long id,
            @Valid @RequestBody AdminVoucherUpdateDTO updateDTO) {
        AdminVoucherDetailDTO updatedVoucher = adminVoucherService.updateVoucher(id, updateDTO);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Voucher updated successfully", updatedVoucher));
    }
}
