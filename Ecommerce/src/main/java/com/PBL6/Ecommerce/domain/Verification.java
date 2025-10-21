package com.PBL6.Ecommerce.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;



@Entity
@Table(name = "verifications")
public class Verification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String contact; // email hoặc sdt
    private String otp;
    private LocalDateTime expiryTime;
    private boolean verified;
    private LocalDateTime createdAt;
    
    // New fields for OTP attempt limiting (Prompt 3)
    private int failedAttempts = 0; // Track failed OTP verification attempts
    private boolean isUsed = false; // Prevent reuse of verified OTPs
    private boolean isLocked = false; // Lock OTP after 3 failed attempts
    private LocalDateTime lastResendTime; // Time-based cooldown for OTP resend (prevents spam)
    // Backwards-compatibility: map the existing DB column `resend_count` so
    // JPA will include a value in INSERTs (database previously failed when
    // column had no default). Initialize to 0.
    @Column(name = "resend_count", nullable = false)
    private int resendCount = 0;

    
    public Verification(String contact, String otp, LocalDateTime expiryTime, boolean verified,
        LocalDateTime createdAt) {
        this.contact = contact;
        this.otp = otp;
        this.expiryTime = expiryTime;
        this.verified = verified;
        this.createdAt = createdAt;
        this.failedAttempts = 0;
        this.isUsed = false;
        this.isLocked = false;
        this.lastResendTime = null;
        this.resendCount = 0;
    }
    public Verification() {
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getContact() {
        return contact;
    }
    public void setContact(String contact) {
        this.contact = contact;
    }
    public String getOtp() {
        return otp;
    }
    public void setOtp(String otp) {
        this.otp = otp;
    }
    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }
    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }
    public boolean isVerified() {
        return verified;
    }
    public void setVerified(boolean verified) {
        this.verified = verified;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Getters and setters for OTP attempt limiting fields
    public int getFailedAttempts() {
        return failedAttempts;
    }
    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }
    public boolean isUsed() {
        return isUsed;
    }
    public void setUsed(boolean used) {
        isUsed = used;
    }
    public boolean isLocked() {
        return isLocked;
    }
    public void setLocked(boolean locked) {
        isLocked = locked;
    }
    
    public LocalDateTime getLastResendTime() {
        return lastResendTime;
    }
    public void setLastResendTime(LocalDateTime lastResendTime) {
        this.lastResendTime = lastResendTime;
    }
    public int getResendCount() {
        return resendCount;
    }
    public void setResendCount(int resendCount) {
        this.resendCount = resendCount;
    }
}