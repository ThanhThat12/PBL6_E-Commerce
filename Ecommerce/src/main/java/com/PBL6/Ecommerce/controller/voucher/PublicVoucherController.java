package com.PBL6.Ecommerce.controller.voucher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.VoucherDTO;
import com.PBL6.Ecommerce.service.VoucherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Public Voucher Controller - APIs công khai cho homepage
 * GET /api/public/vouchers/platform - Lấy voucher do sàn phát hành
 */
@RestController
@RequestMapping("/api/public/vouchers")
@Tag(name = "Public Vouchers", description = "Public APIs for platform vouchers")
public class PublicVoucherController {
    
    private static final Logger log = LoggerFactory.getLogger(PublicVoucherController.class);
    
    private final VoucherService voucherService;

    public PublicVoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    /**
     * Lấy voucher do sàn phát hành (platform vouchers)
     * GET /api/public/vouchers/platform?page=0&size=8
     */
    @Operation(
        summary = "Get platform vouchers for homepage",
        description = "Get active platform vouchers (issued by admin) for homepage display"
    )
    @GetMapping("/platform")
    public ResponseEntity<ResponseDTO<Page<VoucherDTO>>> getPlatformVouchers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<VoucherDTO> vouchers = voucherService.getPlatformVouchers(pageable);
            
            log.info("Found {} platform vouchers for homepage", vouchers.getTotalElements());
            
            return ResponseDTO.success(vouchers, "Lấy danh sách voucher sàn thành công");
        } catch (Exception e) {
            log.error("Error getting platform vouchers", e);
            return ResponseDTO.error(400, "GET_PLATFORM_VOUCHERS_ERROR", e.getMessage());
        }
    }
}
