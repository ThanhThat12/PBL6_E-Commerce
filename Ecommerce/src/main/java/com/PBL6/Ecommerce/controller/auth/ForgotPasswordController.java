package com.PBL6.Ecommerce.controller.auth;

import com.PBL6.Ecommerce.domain.dto.*;
import com.PBL6.Ecommerce.service.ForgotPasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import io.swagger.v3.oas.annotations.tags.Tag;
@Tag(name = "Password Reset", description = "Password reset via email")
@RestController
@RequestMapping("/api/forgot-password")
public class ForgotPasswordController {
    private final ForgotPasswordService forgotPasswordService;

    public ForgotPasswordController(ForgotPasswordService forgotPasswordService) {
        this.forgotPasswordService = forgotPasswordService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ResponseDTO<String>> sendOtp(@RequestBody ForgotPasswordDTO dto) {
        try {
            forgotPasswordService.sendOtp(dto);
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "OTP đã gửi", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(400, e.getMessage(), "Thất bại", null));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ResponseDTO<String>> verifyOtp(@RequestBody VerifyOtpDTO dto) {
        try {
            boolean ok = forgotPasswordService.verifyOtp(dto);
            if (ok) {
                return ResponseEntity.ok(new ResponseDTO<>(200, null, "OTP hợp lệ", null));
            } else {
                return ResponseEntity.badRequest()
                        .body(new ResponseDTO<>(400, "OTP không hợp lệ", "Thất bại", null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(400, e.getMessage(), "Thất bại", null));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<ResponseDTO<String>> resetPassword(@RequestBody ResetPasswordDTO dto) {
        try {
            String result = forgotPasswordService.resetPassword(dto);
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "Thành công", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(400, e.getMessage(), "Thất bại", null));
        }
    }

    /**
     * Admin reset password for user by userId
     * Only ADMIN can access this endpoint
     * 
     * Default passwords based on role:
     * - BUYER/CUSTOMER: Customer123@
     * - SELLER: Seller123@
     * - ADMIN: Admin123@
     * 
     * @param userId - ID of user to reset password
     * @return ResponseDTO with success message and new default password
     */
    @PostMapping("/admin/users/{userId}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<String>> adminResetPassword(@PathVariable Long userId) {
        try {
            String result = forgotPasswordService.adminResetPassword(userId);
            return ResponseEntity.ok(new ResponseDTO<>(
                200, 
                null, 
                "Password reset successfully", 
                result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(
                        400, 
                        e.getMessage(), 
                        "Failed to reset password", 
                        null
                    ));
        }
    }
}
