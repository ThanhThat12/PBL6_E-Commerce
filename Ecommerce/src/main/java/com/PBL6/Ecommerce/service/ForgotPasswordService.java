package com.PBL6.Ecommerce.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.Verification;
import com.PBL6.Ecommerce.domain.dto.ForgotPasswordDTO;
import com.PBL6.Ecommerce.domain.dto.ResetPasswordDTO;
import com.PBL6.Ecommerce.domain.dto.VerifyOtpDTO;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.repository.VerificationRepository;

@Service
public class ForgotPasswordService {
    private final UserRepository userRepository;
    private final VerificationRepository verificationRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService; // Rate limiting service

    // Lưu OTP tạm (có thể thay bằng Redis)
    private final Map<String, String> otpStorage = new HashMap<>();

    public ForgotPasswordService(UserRepository userRepository,
                                 VerificationRepository verificationRepository,
                                 EmailService emailService,
                                 PasswordEncoder passwordEncoder,
                                 LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
    }

    private final ConcurrentMap<String, Object> contactLocks = new ConcurrentHashMap<>();

    // Gửi OTP
    public void sendOtp(ForgotPasswordDTO dto) {
        String contact = dto.getContact();
        
        // Check if OTP resend is rate-limited (Prompt 2)
        if (!loginAttemptService.isOtpResendAllowed(contact)) {
            throw new RuntimeException("Bạn đã gửi OTP quá nhiều lần. Vui lòng thử lại sau 15 phút.");
        }
        
        // Check time-based cooldown: User must wait 60 seconds between OTP requests
        Optional<Verification> lastVerification = verificationRepository
                .findTopByContactOrderByCreatedAtDesc(contact);
        
        if (lastVerification.isPresent()) {
            Verification last = lastVerification.get();
            // Prefer explicit lastResendTime if present, otherwise fallback to createdAt
            java.time.LocalDateTime ref = last.getLastResendTime() != null ? last.getLastResendTime() : last.getCreatedAt();
            // If we can't determine a reliable timestamp, deny resend to be safe and avoid creating duplicate rows
            if (ref == null) {
                throw new RuntimeException("Vui lòng đợi 60 giây trước khi yêu cầu OTP mới.");
            }
            long secondsSinceLastResend = java.time.Duration.between(ref, LocalDateTime.now()).getSeconds();
            if (secondsSinceLastResend < 60) {
                long remainingSeconds = 60 - secondsSinceLastResend;
                throw new RuntimeException("Vui lòng đợi " + remainingSeconds + " giây trước khi yêu cầu OTP mới.");
            }
        }
        
        Optional<User> userOpt = userRepository.findOneByEmail(contact);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Tài khoản không tồn tại");
        }

        String otp = String.format("%06d", new Random().nextInt(999999));

        Object lock = contactLocks.computeIfAbsent(contact, k -> new Object());
        synchronized (lock) {
            // Re-check latest verification inside lock
            Optional<Verification> latest = verificationRepository.findTopByContactOrderByCreatedAtDesc(contact);
            if (latest.isPresent()) {
                Verification l = latest.get();
                java.time.LocalDateTime ref = l.getLastResendTime() != null ? l.getLastResendTime() : l.getCreatedAt();
                if (ref == null) {
                    throw new RuntimeException("Vui lòng đợi 60 giây trước khi yêu cầu OTP mới.");
                }
                long secondsSinceLastResend = java.time.Duration.between(ref, LocalDateTime.now()).getSeconds();
                if (secondsSinceLastResend < 60) {
                    long remainingSeconds = 60 - secondsSinceLastResend;
                    throw new RuntimeException("Vui lòng đợi " + remainingSeconds + " giây trước khi yêu cầu OTP mới.");
                }
            }

            // Save OTP to Verification table for better tracking
            Verification verification = new Verification(
                    contact,
                    otp,
                    LocalDateTime.now().plusMinutes(5),
                    false,
                    LocalDateTime.now()
            );
            verification.setFailedAttempts(0);
            verification.setUsed(false);
            verification.setLocked(false);
            verification.setLastResendTime(LocalDateTime.now()); // Set resend timestamp for cooldown
            verificationRepository.save(verification);

            // Also keep in otpStorage for backward compatibility
            otpStorage.put(contact, otp);

            // Record OTP resend attempt for rate limiting
            loginAttemptService.recordOtpResendAttempt(contact);
        }

        emailService.sendOtp(contact, otp);
    }

    // Xác minh OTP
    public boolean verifyOtp(VerifyOtpDTO dto) {
        String contact = dto.getContact();
        
        // Check if OTP verification is rate-limited (Prompt 2)
        if (!loginAttemptService.isOtpVerifyAllowed(contact)) {
            throw new RuntimeException("Bạn đã xác thực OTP quá nhiều lần. Vui lòng thử lại sau 15 phút.");
        }
        
        Optional<Verification> verificationOpt = verificationRepository
                .findTopByContactOrderByCreatedAtDesc(contact);
        
        if (verificationOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy OTP");
        }
        
        Verification verification = verificationOpt.get();
        
        // Prompt 3: Check if OTP is locked due to too many failed attempts
        if (verification.isLocked()) {
            throw new RuntimeException("OTP này đã bị khóa do nhiều lần xác thực thất bại. Vui lòng yêu cầu OTP mới.");
        }
        
        // Prompt 3: Check if OTP is already used
        if (verification.isUsed()) {
            throw new RuntimeException("OTP này đã được sử dụng rồi. Vui lòng yêu cầu OTP mới.");
        }
        
        String savedOtp = otpStorage.get(contact);
        if (savedOtp == null || !savedOtp.equals(dto.getOtp())) {
            // Prompt 3: Increment failed attempts
            verification.setFailedAttempts(verification.getFailedAttempts() + 1);
            
            // Prompt 3: Lock OTP after 3 failed attempts
            if (verification.getFailedAttempts() >= 3) {
                verification.setLocked(true);
                verificationRepository.save(verification);
                loginAttemptService.recordOtpVerifyAttempt(contact);
                throw new RuntimeException("OTP không đúng. OTP đã bị khóa do 3 lần xác thực thất bại. Vui lòng yêu cầu OTP mới.");
            }
            
            verificationRepository.save(verification);
            loginAttemptService.recordOtpVerifyAttempt(contact);
            int remainingAttempts = 3 - verification.getFailedAttempts();
            throw new RuntimeException("OTP không đúng. Bạn còn " + remainingAttempts + " lần thử.");
        }
        
        // Check OTP expiry
        if (verification.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP đã hết hạn");
        }

        // Prompt 3: Mark OTP as used after successful verification
        verification.setVerified(true);
        verification.setUsed(true);
        verification.setFailedAttempts(0);
        verificationRepository.save(verification);
        
        // Reset rate limiting for this contact after successful verification
        loginAttemptService.resetContactAttempts(contact);
        
        return true;
    }

    // Đặt lại mật khẩu
    public String resetPassword(ResetPasswordDTO dto) {
        String contact = dto.getContact();
        String savedOtp = otpStorage.get(contact);
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

        User user = userRepository.findOneByEmail(contact)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        otpStorage.remove(contact);

        return "Cập nhật mật khẩu thành công";
    }
}
