package com.PBL6.Ecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.dto.CheckContactDTO;
import com.PBL6.Ecommerce.domain.dto.RegisterDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.UserInfoDTO;
import com.PBL6.Ecommerce.domain.dto.VerifyOtpDTO;
import com.PBL6.Ecommerce.service.LoginAttemptService;
import com.PBL6.Ecommerce.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;
    private final LoginAttemptService loginAttemptService; // Prompt 2: Rate limiting

    public UserController(UserService userService, LoginAttemptService loginAttemptService) {
        this.userService = userService;
        this.loginAttemptService = loginAttemptService;
    }
    
    @PostMapping("/register/check-contact")
    public ResponseEntity<ResponseDTO<String>> checkContact(@RequestBody CheckContactDTO dto, HttpServletRequest request) {
        try {
            // Prompt 2: Check global rate limiting
            String clientIp = getClientIp(request);
            if (!loginAttemptService.isGlobalRequestAllowed(clientIp)) {
                ResponseDTO<String> response = new ResponseDTO<>(429, "Too Many Requests", "Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau 1 phút.", null);
                return ResponseEntity.status(429).body(response);
            }
            loginAttemptService.recordGlobalAttempt(clientIp);
            
            String result = userService.checkContact(dto);
            ResponseDTO<String> response = new ResponseDTO<>(200, null, "Thành công", result);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("quá nhiều lần") || e.getMessage().contains("khóa")) {
                ResponseDTO<String> response = new ResponseDTO<>(429, "Too Many Requests", e.getMessage(), null);
                return ResponseEntity.status(429).body(response);
            }
            ResponseDTO<String> response = new ResponseDTO<>(400, e.getMessage(), "Thất bại", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/register/verify-otp")
    public ResponseEntity<ResponseDTO<String>> verifyOtp(@RequestBody VerifyOtpDTO dto, HttpServletRequest request) {
        try {
            // Prompt 2: Check global rate limiting
            String clientIp = getClientIp(request);
            if (!loginAttemptService.isGlobalRequestAllowed(clientIp)) {
                ResponseDTO<String> response = new ResponseDTO<>(429, "Too Many Requests", "Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau 1 phút.", null);
                return ResponseEntity.status(429).body(response);
            }
            loginAttemptService.recordGlobalAttempt(clientIp);
            
            String result = userService.verifyOtp(dto);
            ResponseDTO<String> response = new ResponseDTO<>(200, null, "Thành công", result);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("quá nhiều lần") || e.getMessage().contains("khóa")) {
                ResponseDTO<String> response = new ResponseDTO<>(429, "Too Many Requests", e.getMessage(), null);
                return ResponseEntity.status(429).body(response);
            }
            ResponseDTO<String> response = new ResponseDTO<>(400, e.getMessage(), "Thất bại", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/register/register")
    public ResponseEntity<ResponseDTO<String>> register(@RequestBody RegisterDTO dto, HttpServletRequest request) {
        try {
            // Prompt 2: Check global rate limiting
            String clientIp = getClientIp(request);
            if (!loginAttemptService.isGlobalRequestAllowed(clientIp)) {
                ResponseDTO<String> response = new ResponseDTO<>(429, "Too Many Requests", "Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau 1 phút.", null);
                return ResponseEntity.status(429).body(response);
            }
            loginAttemptService.recordGlobalAttempt(clientIp);
            
            String result = userService.register(dto);
            ResponseDTO<String> response = new ResponseDTO<>(200, null, "Thành công", result);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ResponseDTO<String> response = new ResponseDTO<>(400, e.getMessage(), "Thất bại", null);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/user/me")
    public ResponseEntity<ResponseDTO<UserInfoDTO>> getCurrentUser() {
        try {
            UserInfoDTO userInfo = userService.getCurrentUser();
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "Lấy thông tin thành công", userInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Thất bại", null)
            );
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
