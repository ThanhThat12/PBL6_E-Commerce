package com.PBL6.Ecommerce.controller.voucher;

import com.PBL6.Ecommerce.domain.dto.ApplyVoucherRequestDTO;
import com.PBL6.Ecommerce.domain.dto.CreateVoucherRequestDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.VoucherApplicationResultDTO;
import com.PBL6.Ecommerce.domain.dto.VoucherDTO;
import com.PBL6.Ecommerce.service.VoucherService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for Voucher Management
 * 
 * Endpoints:
 * POST /api/seller/vouchers - Tạo voucher mới (SELLER only)
 * GET /api/seller/vouchers - Lấy tất cả voucher của shop (SELLER only)
 * GET /api/seller/vouchers/active - Lấy voucher đang active (SELLER only)
 * PATCH /api/seller/vouchers/{id}/deactivate - Vô hiệu hóa voucher (SELLER only)
 */
@RestController
@RequestMapping("/api/seller/vouchers")
public class VoucherController {
    
    private static final Logger log = LoggerFactory.getLogger(VoucherController.class);
    
    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    /**
     * Tạo voucher mới cho shop
     * POST /api/seller/vouchers
     * Body: CreateVoucherRequestDTO
     */
    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<VoucherDTO>> createVoucher(
            @Valid @RequestBody CreateVoucherRequestDTO request,
            Authentication authentication) {
        try {
            VoucherDTO voucher = voucherService.createVoucher(request, authentication);
            return ResponseDTO.created(voucher, "Tạo voucher thành công");
        } catch (Exception e) {
            return ResponseDTO.error(400, "CREATE_VOUCHER_ERROR", e.getMessage());
        }
    }

    /**
     * Lấy tất cả voucher của shop
     * GET /api/seller/vouchers
     */
    @GetMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<List<VoucherDTO>>> getShopVouchers(Authentication authentication) {
        try {
            List<VoucherDTO> vouchers = voucherService.getShopVouchers(authentication);
            return ResponseDTO.success(vouchers, "Lấy danh sách voucher thành công");
        } catch (Exception e) {
            return ResponseDTO.error(400, "GET_VOUCHERS_ERROR", e.getMessage());
        }
    }

    /**
     * Lấy voucher đang active của shop
     * GET /api/seller/vouchers/active
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<List<VoucherDTO>>> getActiveShopVouchers(Authentication authentication) {
        try {
            List<VoucherDTO> vouchers = voucherService.getActiveShopVouchers(authentication);
            return ResponseDTO.success(vouchers, "Lấy danh sách voucher đang active thành công");
        } catch (Exception e) {
            return ResponseDTO.error(400, "GET_ACTIVE_VOUCHERS_ERROR", e.getMessage());
        }
    }

    /**
     * Vô hiệu hóa voucher
     * PATCH /api/seller/vouchers/{id}/deactivate
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<VoucherDTO>> deactivateVoucher(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            VoucherDTO voucher = voucherService.deactivateVoucher(id, authentication);
            return ResponseDTO.success(voucher, "Vô hiệu hóa voucher thành công");
        } catch (Exception e) {
            return ResponseDTO.error(400, "DEACTIVATE_VOUCHER_ERROR", e.getMessage());
        }
    }

    /**
     * Lấy danh sách voucher khả dụng cho người dùng
     * GET /api/seller/vouchers/available?shopId=1&productIds=1,2,3&cartTotal=500000
     * shopId is optional - if not provided, will be determined from productIds
     */
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('USER', 'BUYER', 'SELLER')")
    public ResponseEntity<ResponseDTO<List<VoucherDTO>>> getAvailableVouchers(
            @RequestParam(required = false) Long shopId,
            @RequestParam String productIds,
            @RequestParam BigDecimal cartTotal,
            Authentication authentication) {
        try {
            // Parse productIds
            List<Long> productIdList = Arrays.stream(productIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
            
            // Lấy username từ authentication
            String username = authentication.getName();
            
            log.info("Getting available vouchers: shopId={}, username={}, productIds={}, cartTotal={}", 
                     shopId, username, productIdList, cartTotal);
            
            List<VoucherDTO> availableVouchers = voucherService.getAvailableVouchersForUser(
                shopId, username, productIdList, cartTotal);
            
            log.info("Found {} available vouchers", availableVouchers.size());
            
            ResponseDTO<List<VoucherDTO>> response = new ResponseDTO<>(
                200, null, "Lấy danh sách voucher khả dụng thành công", availableVouchers);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting available vouchers", e);
            ResponseDTO<List<VoucherDTO>> response = new ResponseDTO<>(
                400, "GET_AVAILABLE_VOUCHERS_ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }

/**
 * Áp dụng voucher cho đơn hàng
 * POST /api/seller/vouchers/apply
 */
@PostMapping("/apply")
@PreAuthorize("hasAnyRole('USER', 'SELLER')")
public ResponseEntity<ResponseDTO<VoucherApplicationResultDTO>> applyVoucher(
        @Valid @RequestBody ApplyVoucherRequestDTO request,
        Authentication authentication) {
    try {
        String username = authentication.getName();
        VoucherApplicationResultDTO result = voucherService.applyVoucher(
            request.getVoucherCode(), request.getProductIds(), request.getCartTotal(), username);
        
        return ResponseDTO.success(result, "Áp dụng voucher thành công");
    } catch (Exception e) {
        return ResponseDTO.error(400, "APPLY_VOUCHER_ERROR", e.getMessage());
    }
}

/**
 * Lấy danh sách khách hàng đã từng mua hàng của shop (để chọn khi tạo voucher SPECIFIC_USERS)
 * GET /api/seller/vouchers/customers
 */
@GetMapping("/customers")
@PreAuthorize("hasRole('SELLER')")
public ResponseEntity<ResponseDTO<List<Map<String, Object>>>> getShopCustomers(Authentication authentication) {
    try {
        List<Map<String, Object>> customers = voucherService.getShopCustomers(authentication);
        return ResponseDTO.success(customers, "Lấy danh sách khách hàng thành công");
    } catch (Exception e) {
        return ResponseDTO.error(400, "GET_CUSTOMERS_ERROR", e.getMessage());
    }
}
}