package com.PBL6.Ecommerce.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.PBL6.Ecommerce.domain.Role;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.Verification;
import com.PBL6.Ecommerce.domain.dto.CheckContactDTO;
import com.PBL6.Ecommerce.domain.dto.RegisterDTO;
import com.PBL6.Ecommerce.domain.dto.UserInfoDTO;
import com.PBL6.Ecommerce.domain.dto.VerifyOtpDTO;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.repository.VerificationRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final VerificationRepository verificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;     // gửi email OTP
    private final SmsService smsService;         // gửi SMS OTP
    private final LoginAttemptService loginAttemptService; // Rate limiting service

    public UserService(UserRepository userRepository,
                       VerificationRepository verificationRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService,
                       SmsService smsService,
                       LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.smsService = smsService;
        this.loginAttemptService = loginAttemptService;
    }

    // Locks per contact to avoid concurrent verification insertions
    private final ConcurrentMap<String, Object> contactLocks = new ConcurrentHashMap<>();

    public String checkContact(CheckContactDTO dto) {
        String contact = dto.getContact();

        // Check if contact is rate-limited for OTP resend
        if (!loginAttemptService.isOtpResendAllowed(contact)) {
            throw new RuntimeException("Bạn đã gửi OTP quá nhiều lần. Vui lòng thử lại sau 15 phút.");
        }

        // Time-based cooldown: prevent quick consecutive resends (60 seconds)
        Optional<Verification> lastOpt = verificationRepository.findTopByContactOrderByCreatedAtDesc(contact);
        if (lastOpt.isPresent()) {
            Verification last = lastOpt.get();
            // Prefer explicit lastResendTime if present, otherwise fallback to createdAt (backwards compatibility)
            java.time.LocalDateTime ref = last.getLastResendTime() != null ? last.getLastResendTime() : last.getCreatedAt();
            // If we can't determine a reliable timestamp, deny resend to be safe and avoid creating duplicate rows
            if (ref == null) {
                throw new RuntimeException("Vui lòng đợi 60 giây trước khi yêu cầu mã OTP mới.");
            }
            long secondsSince = java.time.Duration.between(ref, LocalDateTime.now()).getSeconds();
            if (secondsSince < 60) {
                throw new RuntimeException("Vui lòng đợi " + (60 - secondsSince) + " giây trước khi yêu cầu mã OTP mới.");
            }
        }

        // 1. Kiểm tra tồn tại
        if (contact.contains("@")) {
            if (userRepository.existsByEmail(contact)) {
                throw new RuntimeException("Email đã tồn tại");
            }
        } else {
            if (userRepository.existsByPhoneNumber(contact)) {
                throw new RuntimeException("Số điện thoại đã tồn tại");
            }
        }


        // 2. Sinh OTP and save inside a per-contact synchronized block to avoid duplicate DB inserts
        String otp = String.format("%06d", new Random().nextInt(999999));

        Object lock = contactLocks.computeIfAbsent(contact, k -> new Object());
        synchronized (lock) {
            // Re-check latest verification inside lock to avoid race condition
            Optional<Verification> latest = verificationRepository.findTopByContactOrderByCreatedAtDesc(contact);
            if (latest.isPresent()) {
                Verification l = latest.get();
                java.time.LocalDateTime ref = l.getLastResendTime() != null ? l.getLastResendTime() : l.getCreatedAt();
                if (ref == null) {
                    throw new RuntimeException("Vui lòng đợi 60 giây trước khi yêu cầu mã OTP mới.");
                }
                long secondsSince = java.time.Duration.between(ref, LocalDateTime.now()).getSeconds();
                if (secondsSince < 60) {
                    throw new RuntimeException("Vui lòng đợi " + (60 - secondsSince) + " giây trước khi yêu cầu mã OTP mới.");
                }
            }

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
            verification.setLastResendTime(LocalDateTime.now()); // set cooldown timestamp
            verificationRepository.save(verification);

            // Record OTP resend attempt for rate limiting
            loginAttemptService.recordOtpResendAttempt(contact);
        }

        // 3. Gửi OTP
        if (contact.contains("@")) {
            emailService.sendOtp(contact, otp);
        } else {
            smsService.sendOtp(contact, otp);
        }

        return "OTP đã được gửi tới " + contact;
    }

    public String verifyOtp(VerifyOtpDTO dto) {
        String contact = dto.getContact();
        
        // Check if OTP verification is rate-limited (Prompt 2)
        if (!loginAttemptService.isOtpVerifyAllowed(contact)) {
            throw new RuntimeException("Bạn đã xác thực OTP quá nhiều lần. Vui lòng thử lại sau 15 phút.");
        }
        
        Optional<Verification> verificationOpt =
                verificationRepository.findTopByContactOrderByCreatedAtDesc(contact);

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
        
        // Check OTP value
        if (!verification.getOtp().equals(dto.getOtp())) {
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
        verification.setFailedAttempts(0); // Reset failed attempts on success
        verificationRepository.save(verification);
        
        // Reset rate limiting for this contact after successful verification
        loginAttemptService.resetContactAttempts(contact);

        return "Xác thực thành công";
    }

    public String register(RegisterDTO dto) {
        Verification verification = verificationRepository
                .findTopByContactOrderByCreatedAtDesc(dto.getContact())
                .orElseThrow(() -> new RuntimeException("Chưa xác thực OTP"));

        if (!verification.isVerified()) {
            throw new RuntimeException("Bạn chưa xác thực OTP");
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu không khớp");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        if (dto.getContact().contains("@")) {
            user.setEmail(dto.getContact());
        } else {
            user.setPhoneNumber(dto.getContact());
        }

        user.setActivated(true);
        user.setRole(Role.BUYER);

        userRepository.save(user);

        return "Đăng ký thành công";
    }
    // cập nhật mật khẩu cho email hoặc phone
    public void updatePassword(String contact, String newPassword) {
        User user = null;

        if (contact.contains("@")) { // nếu là email
            user = userRepository.findOneByEmail(contact)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user với email: " + contact));
        } else { // nếu là số điện thoại
            user = userRepository.findOneByPhoneNumber(contact)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user với số điện thoại: " + contact));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public UserInfoDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String principal = authentication.getName();

        Optional<User> userOpt = userRepository.findOneByEmail(principal);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findOneByUsername(principal);
        }
        User user = userOpt.orElseThrow(() -> new RuntimeException("User not found"));

        return new UserInfoDTO(user.getId(), user.getEmail(), user.getUsername(), user.getPhoneNumber(), user.getRole().name());
    }
}
