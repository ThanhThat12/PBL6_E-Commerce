package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.Verification;
import com.PBL6.Ecommerce.domain.Role;
import com.PBL6.Ecommerce.domain.dto.CheckContactDTO;
import com.PBL6.Ecommerce.domain.dto.VerifyOtpDTO;
import com.PBL6.Ecommerce.domain.dto.RegisterDTO;
import com.PBL6.Ecommerce.domain.dto.UserInfoDTO;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.repository.VerificationRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final VerificationRepository verificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;     // gửi email OTP
    private final SmsService smsService;         // gửi SMS OTP

    public UserService(UserRepository userRepository,
                       VerificationRepository verificationRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService,
                       SmsService smsService) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.smsService = smsService;
    }

    public String checkContact(CheckContactDTO dto) {
        String contact = dto.getContact();

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

        // 2. Sinh OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        Verification verification = new Verification(
                contact,
                otp,
                LocalDateTime.now().plusMinutes(5),
                false,
                LocalDateTime.now()
        );
        verificationRepository.save(verification);

        // 3. Gửi OTP
        if (contact.contains("@")) {
            emailService.sendOtp(contact, otp);
        } else {
            smsService.sendOtp(contact, otp);
        }

        return "OTP đã được gửi tới " + contact;
    }

    public String verifyOtp(VerifyOtpDTO dto) {
        Optional<Verification> verificationOpt =
                verificationRepository.findTopByContactOrderByCreatedAtDesc(dto.getContact());

        if (verificationOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy OTP");
        }

        Verification verification = verificationOpt.get();
        if (!verification.getOtp().equals(dto.getOtp())) {
            throw new RuntimeException("OTP không đúng");
        }
        if (verification.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP đã hết hạn");
        }

        verification.setVerified(true);
        verificationRepository.save(verification);

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

        return new UserInfoDTO(user.getId(), user.getEmail(), user.getUsername(), user.getRole().name());
    }
}