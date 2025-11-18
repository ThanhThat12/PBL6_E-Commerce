package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Role;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.ForgotPasswordDTO;
import com.PBL6.Ecommerce.domain.dto.ResetPasswordDTO;
import com.PBL6.Ecommerce.domain.dto.VerifyOtpDTO;
import com.PBL6.Ecommerce.exception.UserNotFoundException;
import com.PBL6.Ecommerce.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class ForgotPasswordService {
    private static final Logger log = LoggerFactory.getLogger(ForgotPasswordService.class);
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // Lưu OTP tạm (có thể thay bằng Redis)
    private final Map<String, String> otpStorage = new HashMap<>();

    public ForgotPasswordService(UserRepository userRepository,
                                 EmailService emailService,
                                 PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    // Gửi OTP
    public void sendOtp(ForgotPasswordDTO dto) {
        Optional<User> userOpt = userRepository.findOneByEmail(dto.getContact());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Tài khoản không tồn tại");
        }

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        otpStorage.put(dto.getContact(), otp);

        emailService.sendOtp(dto.getContact(), otp);
    }

    // Xác minh OTP
    public boolean verifyOtp(VerifyOtpDTO dto) {
        String savedOtp = otpStorage.get(dto.getContact());
        return savedOtp != null && savedOtp.equals(dto.getOtp());
    }

    // Đặt lại mật khẩu
    public String resetPassword(ResetPasswordDTO dto) {
        String savedOtp = otpStorage.get(dto.getContact());
        if (savedOtp == null || !savedOtp.equals(dto.getOtp())) {
            throw new RuntimeException("OTP không hợp lệ hoặc đã hết hạn");
        }

        if (dto.getNewPassword() != null
        && dto.getConfirmNewPassword() != null
        && dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
    // mật khẩu trùng khớp
} else {
    throw new RuntimeException("Mật khẩu mới và xác nhận mật khẩu không khớp hoặc bị thiếu");
}

        User user = userRepository.findOneByEmail(dto.getContact())
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        otpStorage.remove(dto.getContact());

        return "Cập nhật mật khẩu thành công";
    }

    /**
     * Admin reset password for user by userId
     * Default password based on role:
     * - BUYER/CUSTOMER: Customer123@
     * - SELLER: Seller123@
     * - ADMIN: Admin123@
     * 
     * @param userId - ID của user cần reset password
     * @return Message xác nhận reset thành công
     */
    @Transactional
    public String adminResetPassword(Long userId) {
        log.debug("Admin resetting password for user ID: {}", userId);
        
        // Tìm user theo ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found with ID: " + userId);
                });
        
        // Xác định mật khẩu mặc định dựa trên role
        String defaultPassword;
        switch (user.getRole()) {
            case BUYER:
                defaultPassword = "Customer123@";
                break;
            case SELLER:
                defaultPassword = "Seller123@";
                break;
            case ADMIN:
                defaultPassword = "Admin123@";
                break;
            default:
                defaultPassword = "User123@";
                break;
        }
        
        // Mã hóa và cập nhật mật khẩu
        user.setPassword(passwordEncoder.encode(defaultPassword));
        userRepository.save(user);
        
        log.info("Password reset successfully for user ID: {}, Username: {}, Role: {}", 
            userId, user.getUsername(), user.getRole());
        
        return "Password has been reset to default: " + defaultPassword;
    }
}
