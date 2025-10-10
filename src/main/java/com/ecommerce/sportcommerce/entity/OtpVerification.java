package com.ecommerce.sportcommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * OTP Verification Entity
 * Stores OTP codes for various verification purposes
 */
@Entity
@Table(name = "otp_verifications",
       indexes = {
           @Index(name = "idx_email_type", columnList = "email, otp_type"),
           @Index(name = "idx_expires_at", columnList = "expires_at"),
           @Index(name = "idx_verified", columnList = "verified")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String email;
    
    @Column(name = "otp_code", nullable = false, length = 6)
    private String otpCode;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "otp_type", nullable = false, length = 20)
    private OtpType otpType;
    
    // Security
    @Column(nullable = false)
    @Builder.Default
    private Boolean verified = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer attempts = 0;
    
    @Column(name = "max_attempts", nullable = false)
    @Builder.Default
    private Integer maxAttempts = 5;
    
    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
    // Optional user link
    @Column(name = "user_id")
    private Long userId;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Enums
    public enum OtpType {
        REGISTRATION,
        PASSWORD_RESET,
        EMAIL_CHANGE,
        LOGIN_2FA
    }
    
    /**
     * Check if OTP is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Check if max attempts reached
     */
    public boolean isMaxAttemptsReached() {
        return attempts >= maxAttempts;
    }
}
