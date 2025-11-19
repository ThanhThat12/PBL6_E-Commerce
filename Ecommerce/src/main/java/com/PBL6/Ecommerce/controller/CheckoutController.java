package com.PBL6.Ecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import com.PBL6.Ecommerce.domain.dto.*;
import com.PBL6.Ecommerce.service.CheckoutService;

import java.util.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    /**
     * Bước 1-6: User ấn thanh toán → Backend trả danh sách dịch vụ GHN
     * POST /api/checkout/available-services
     */
    @PostMapping("/available-services")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<List<Map<String,Object>>>> getAvailableServices(
            @Valid @RequestBody CheckoutInitRequestDTO req,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            String email = jwt.getClaimAsString("email");
            if (email == null) {
                throw new RuntimeException("Token không có email");
            }
            
            // ✅ Delegate to CheckoutService
            List<Map<String,Object>> services = checkoutService.getAvailableShippingServices(email, req);
            
            return ResponseDTO.ok(services, "Lấy danh sách dịch vụ thành công");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(new ResponseDTO<>(400, e.getMessage(), "Lỗi", null));
        }
    }

    /**
     * Bước 7-10: User chọn service → Backend tính phí ship
     * POST /api/checkout/calculate-fee
     */
    @PostMapping("/calculate-fee")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<Map<String,Object>>> calculateShippingFee(
            @Valid @RequestBody CheckoutCalculateFeeRequestDTO req,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            String email = jwt.getClaimAsString("email");
            if (email == null) {
                throw new RuntimeException("Token không có email");
            }
            
            // ✅ Delegate to CheckoutService
            Map<String,Object> feeResponse = checkoutService.calculateShippingFee(email, req);
            
            return ResponseDTO.ok(feeResponse, "Tính phí thành công");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(new ResponseDTO<>(400, e.getMessage(), "Lỗi", null));
        }
    }

    /**
     * Bước 11-13: User xác nhận thanh toán → Tạo Order + GHN shipment
     * POST /api/checkout/confirm
     */
    @PostMapping("/confirm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<Map<String,Object>>> confirmCheckout(
            @Valid @RequestBody CheckoutConfirmRequestDTO req,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            String email = jwt.getClaimAsString("email");
            if (email == null) {
                throw new RuntimeException("Token không có email");
            }
            
            // ✅ Delegate to CheckoutService
            Map<String,Object> result = checkoutService.confirmCheckout(email, req);
            
            return ResponseDTO.ok(result, "Tạo đơn hàng thành công");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(new ResponseDTO<>(400, e.getMessage(), "Lỗi tạo đơn", null));
        }
    }
}
