package com.PBL6.Ecommerce.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Refresh Token Entity for implementing token rotation and revocation
 * Stores long-lived refresh tokens used to obtain new access tokens
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 500)
    private String token; // UUID or secure random string
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    // Map to DB `created_at` column (database has created_at, JPA default would be created_date)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @Column(length = 45)
    private String ipAddress; // For security tracking (IPv4/IPv6)
    
    @Column(length = 500)
    private String userAgent; // Device/browser info
    
    @Column(nullable = false)
    private boolean revoked = false; // Token revocation flag
    
    // Constructors
    public RefreshToken() {
        this.createdDate = LocalDateTime.now();
    }
    
    public RefreshToken(String token, User user, LocalDateTime expiryDate, String ipAddress, String userAgent) {
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdDate = LocalDateTime.now();
        this.revoked = false;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public boolean isRevoked() {
        return revoked;
    }
    
    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
    
    /**
     * Check if refresh token is still valid (not expired and not revoked)
     */
    public boolean isValid() {
        return !revoked && LocalDateTime.now().isBefore(expiryDate);
    }
    
    /**
     * Check if refresh token has expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
