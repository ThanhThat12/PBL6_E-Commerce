package com.PBL6.Ecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.dto.ForgotPasswordDTO;
import com.PBL6.Ecommerce.domain.dto.ResetPasswordDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.VerifyOtpDTO;
import com.PBL6.Ecommerce.service.ForgotPasswordService;
import com.PBL6.Ecommerce.service.LoginAttemptService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/forgot-password")
public class ForgotPasswordController {
    private final ForgotPasswordService forgotPasswordService;
    private final LoginAttemptService loginAttemptService; // Prompt 2: Rate limiting

    public ForgotPasswordController(ForgotPasswordService forgotPasswordService,
                                   LoginAttemptService loginAttemptService) {
        this.forgotPasswordService = forgotPasswordService;
        this.loginAttemptService = loginAttemptService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ResponseDTO<String>> sendOtp(@RequestBody ForgotPasswordDTO dto, HttpServletRequest request) {
        try {
            // Prompt 2: Check global rate limiting
            String clientIp = getClientIp(request);
            if (!loginAttemptService.isGlobalRequestAllowed(clientIp)) {
                ResponseDTO<String> response = new ResponseDTO<>(429, "Too Many Requests", "Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau 1 phút.", null);
                return ResponseEntity.status(429).body(response);
            }
            loginAttemptService.recordGlobalAttempt(clientIp);
            
            forgotPasswordService.sendOtp(dto);
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "OTP đã gửi", null));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("quá nhiều lần") || e.getMessage().contains("khóa")) {
                ResponseDTO<String> response = new ResponseDTO<>(429, "Too Many Requests", e.getMessage(), null);
                return ResponseEntity.status(429).body(response);
            }
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(400, e.getMessage(), "Thất bại", null));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ResponseDTO<String>> verifyOtp(@RequestBody VerifyOtpDTO dto, HttpServletRequest request) {
        try {
            // Prompt 2: Check global rate limiting
            String clientIp = getClientIp(request);
            if (!loginAttemptService.isGlobalRequestAllowed(clientIp)) {
                ResponseDTO<String> response = new ResponseDTO<>(429, "Too Many Requests", "Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau 1 phút.", null);
                return ResponseEntity.status(429).body(response);
            }
            loginAttemptService.recordGlobalAttempt(clientIp);
            
            boolean ok = forgotPasswordService.verifyOtp(dto);
            if (ok) {
                return ResponseEntity.ok(new ResponseDTO<>(200, null, "OTP hợp lệ", null));
            } else {
                return ResponseEntity.badRequest()
                        .body(new ResponseDTO<>(400, "OTP không hợp lệ", "Thất bại", null));
            }
        } catch (RuntimeException e) {
            if (e.getMessage().contains("quá nhiều lần") || e.getMessage().contains("khóa")) {
                ResponseDTO<String> response = new ResponseDTO<>(429, "Too Many Requests", e.getMessage(), null);
                return ResponseEntity.status(429).body(response);
            }
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(400, e.getMessage(), "Thất bại", null));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<ResponseDTO<String>> resetPassword(@RequestBody ResetPasswordDTO dto, HttpServletRequest request) {
        try {
            // Prompt 2: Check global rate limiting
            String clientIp = getClientIp(request);
            if (!loginAttemptService.isGlobalRequestAllowed(clientIp)) {
                ResponseDTO<String> response = new ResponseDTO<>(429, "Too Many Requests", "Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau 1 phút.", null);
                return ResponseEntity.status(429).body(response);
            }
            loginAttemptService.recordGlobalAttempt(clientIp);
            
            String result = forgotPasswordService.resetPassword(dto);
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "Thành công", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(400, e.getMessage(), "Thất bại", null));
        }
    }
    
    /**
     * Extract client IP address from request, handling proxies
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
