package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.ForgotPasswordDTO;
import com.PBL6.Ecommerce.domain.dto.ResetPasswordDTO;
import com.PBL6.Ecommerce.domain.dto.VerifyOtpDTO;
import com.PBL6.Ecommerce.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class ForgotPasswordService {
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
}
