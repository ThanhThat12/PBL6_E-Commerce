package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.domain.Vouchers;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherCreateDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherDetailDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherListDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminVoucherStatsDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminUpdateVoucherDTO;
import com.PBL6.Ecommerce.service.AdminVoucherService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminVoucherController {
    
    private final AdminVoucherService adminVoucherService;

    public AdminVoucherController(AdminVoucherService adminVoucherService) {
        this.adminVoucherService = adminVoucherService;
    }

    /**
     * API lấy danh sách tất cả voucher có phân trang
     * GET /api/admin/vouchers/page?page=0&size=10
     * @param page - Số trang (bắt đầu từ 0)
     * @param size - Số lượng voucher mỗi trang (mặc định 10)
     * @return ResponseDTO<Page<AdminVoucherListDTO>>
     */
    @GetMapping("/vouchers/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<AdminVoucherListDTO>>> getAllVouchersWithPaging(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<AdminVoucherListDTO> vouchersPage = adminVoucherService.getAllVouchers(page, size);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Vouchers retrieved successfully", vouchersPage));
    }

    /**
     * API xem chi tiết voucher
     * GET /api/admin/voucher/{id}
     * @param id - ID của voucher
     * @return ResponseDTO<AdminVoucherDetailDTO>
     */
    @GetMapping("/voucher/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminVoucherDetailDTO>> getVoucherDetail(@PathVariable Long id) {
        AdminVoucherDetailDTO voucherDetail = adminVoucherService.getVoucherDetail(id);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Voucher detail retrieved successfully", voucherDetail));
    }

    /**
     * API thêm voucher mới
     * POST /api/admin/voucher/add
     * @param dto - AdminVoucherCreateDTO
     * @return ResponseDTO<Vouchers>
     */
    @PostMapping("/voucher/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Vouchers>> createVoucher(@RequestBody AdminVoucherCreateDTO dto) {
        Vouchers voucher = adminVoucherService.createVoucher(dto);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Voucher created successfully", voucher));
    }

    /**
     * API sửa voucher
     * PUT /api/admin/voucher/{id}/update
     * @param id - ID của voucher
     * @param dto - AdminUpdateVoucherDTO
     * @return ResponseDTO<Vouchers>
     */
    @PutMapping("/voucher/{id}/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Vouchers>> updateVoucher(
            @PathVariable Long id,
            @RequestBody AdminUpdateVoucherDTO dto) {
        Vouchers voucher = adminVoucherService.updateVoucher(id, dto);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Voucher updated successfully", voucher));
    }

    /**
     * API xóa voucher
     * DELETE /api/admin/voucher/{id}/delete
     * @param id - ID của voucher
     * @return ResponseDTO<String>
     */
    @DeleteMapping("/voucher/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<String>> deleteVoucher(@PathVariable Long id) {
        adminVoucherService.deleteVoucher(id);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Voucher deleted successfully", "Voucher has been removed from the system"));
    }
    
    /**
     * API lấy thống kê voucher
     * GET /api/admin/vouchers/stats
     * @return ResponseDTO<AdminVoucherStatsDTO>
     */
    @GetMapping("/vouchers/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminVoucherStatsDTO>> getVoucherStats() {
        AdminVoucherStatsDTO stats = adminVoucherService.getVoucherStats();
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Voucher statistics retrieved successfully", stats));
    }
}

