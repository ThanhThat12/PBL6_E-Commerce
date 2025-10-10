package com.ecommerce.sportcommerce.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.sportcommerce.entity.OtpVerification;
import com.ecommerce.sportcommerce.exception.BadRequestException;
import com.ecommerce.sportcommerce.repository.OtpVerificationRepository;
import com.ecommerce.sportcommerce.util.OtpGenerator;

/**
 * Service for OTP operations
 */
@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    private final OtpVerificationRepository otpRepository;
    private final EmailService emailService;

    @Value("${otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    @Value("${otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${otp.resend-cooldown-seconds:60}")
    private int resendCooldownSeconds;

    public OtpService(OtpVerificationRepository otpRepository, EmailService emailService) {
        this.otpRepository = otpRepository;
        this.emailService = emailService;
    }

    /**
     * Generate and save OTP
     */
    @Transactional
    public OtpVerification generateOtp(String email, OtpVerification.OtpType otpType) {
        // Check rate limiting for resend
        checkRateLimit(email, otpType);

        // Delete old unverified OTPs
        otpRepository.deleteUnverifiedOtpsByEmailAndType(email, otpType);

        // Generate new OTP
        String otpCode = OtpGenerator.generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpirationMinutes);

        OtpVerification otp = OtpVerification.builder()
                .email(email)
                .otpCode(otpCode)
                .otpType(otpType)
                .verified(false)
                .attempts(0)
                .maxAttempts(maxAttempts)
                .expiresAt(expiresAt)
                .build();

        OtpVerification saved = otpRepository.save(otp);
        logger.info("Generated OTP for email: {}, type: {}", email, otpType);

        return saved;
    }

    /**
     * Generate OTP and send it to the user's email.
     * This is a convenience method used by controllers/services that need both actions.
     */
    @Transactional
    public OtpVerification generateAndSendOtp(String email, OtpVerification.OtpType otpType) {
        OtpVerification otp = generateOtp(email, otpType);

        // Use EmailService to send the OTP. For registration and similar flows use sendOtpEmail.
        try {
            emailService.sendOtpEmail(email, otp.getOtpCode(), otpExpirationMinutes);
            logger.info("OTP generated and email sent to: {}", email);
        } catch (RuntimeException ex) {
            logger.error("Failed to send OTP email to: {}", email, ex);
            // You may decide to rethrow or swallow depending on requirement; rethrow to let caller handle it
            throw ex;
        }

        return otp;
    }

    /**
     * Verify OTP
     */
    @Transactional
    public OtpVerification verifyOtp(String email, String otpCode, OtpVerification.OtpType otpType) {
        Optional<OtpVerification> otpOptional = otpRepository.findLatestValidOtp(
                email, otpType, LocalDateTime.now());

        if (otpOptional.isEmpty()) {
            logger.warn("No valid OTP found for email: {}, type: {}", email, otpType);
            throw new BadRequestException("OTP không hợp lệ hoặc đã hết hạn");
        }

        OtpVerification otp = otpOptional.get();

        // Check if already verified
        if (otp.getVerified()) {
            throw new BadRequestException("OTP đã được xác thực trước đó");
        }

        // Check max attempts
        if (otp.isMaxAttemptsReached()) {
            logger.warn("Max OTP attempts reached for email: {}", email);
            throw new BadRequestException("Bạn đã nhập sai OTP quá nhiều lần. Vui lòng yêu cầu OTP mới");
        }

        // Check expiration
        if (otp.isExpired()) {
            throw new BadRequestException("OTP đã hết hạn. Vui lòng yêu cầu OTP mới");
        }

        // Verify OTP code
        if (!otp.getOtpCode().equals(otpCode)) {
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepository.save(otp);
            logger.warn("Invalid OTP attempt for email: {}, attempts: {}", email, otp.getAttempts());
            throw new BadRequestException("Mã OTP không chính xác. Còn " +
                    (maxAttempts - otp.getAttempts()) + " lần thử");
        }

        // Mark as verified
        otp.setVerified(true);
        otp.setVerifiedAt(LocalDateTime.now());
        otpRepository.save(otp);

        logger.info("OTP verified successfully for email: {}", email);
        return otp;
    }

    /**
     * Check rate limiting for resend OTP
     */
    private void checkRateLimit(String email, OtpVerification.OtpType otpType) {
        LocalDateTime cooldownTime = LocalDateTime.now().minusSeconds(resendCooldownSeconds);
        long recentOtpCount = otpRepository.countRecentOtps(email, otpType, cooldownTime);

        if (recentOtpCount > 0) {
            logger.warn("Rate limit exceeded for email: {}", email);
            throw new BadRequestException("Vui lòng đợi " + resendCooldownSeconds +
                    " giây trước khi gửi lại OTP");
        }
    }

    /**
     * Clean up expired OTPs (scheduled task)
     */
    @Transactional
    public int cleanupExpiredOtps() {
        int deletedCount = otpRepository.deleteExpiredOtps(LocalDateTime.now());
        if (deletedCount > 0) {
            logger.info("Cleaned up {} expired OTPs", deletedCount);
        }
        return deletedCount;
    }

    /**
     * Get OTP expiration time in minutes
     */
    public int getOtpExpirationMinutes() {
        return otpExpirationMinutes;
    }
}
