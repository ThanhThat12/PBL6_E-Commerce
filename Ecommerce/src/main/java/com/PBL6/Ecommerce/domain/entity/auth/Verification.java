package com.PBL6.Ecommerce.domain.entity.auth;

import java.time.LocalDateTime;

import jakarta.persistence.*;



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

    
    public Verification(String contact, String otp, LocalDateTime expiryTime, boolean verified,
        LocalDateTime createdAt) {
        this.contact = contact;
        this.otp = otp;
        this.expiryTime = expiryTime;
        this.verified = verified;
        this.createdAt = createdAt;
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
}

