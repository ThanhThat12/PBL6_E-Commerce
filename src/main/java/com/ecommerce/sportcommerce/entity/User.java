package com.ecommerce.sportcommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * User Entity
 * Represents user accounts in the e-commerce platform
 */
@Entity
@Table(name = "users", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_email_provider", columnNames = {"email", "provider"}),
           @UniqueConstraint(name = "uk_username", columnNames = {"username"}),
           @UniqueConstraint(name = "uk_phone", columnNames = {"phone"})
       },
       indexes = {
           @Index(name = "idx_role", columnList = "role"),
           @Index(name = "idx_status", columnList = "status"),
           @Index(name = "idx_email", columnList = "email")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Basic Information
    @Column(nullable = false, length = 255)
    private String email;
    
    @Column(length = 255)
    private String password; // Nullable for OAuth users
    
    @Column(length = 100)
    private String username;
    
    @Column(name = "first_name", length = 100)
    private String firstName;
    
    @Column(name = "last_name", length = 100)
    private String lastName;
    
    @Column(length = 20)
    private String phone;
    
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;
    
    // Role & Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private Role role = Role.BUYER;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private Status status = Status.ACTIVE;
    
    // OAuth Fields
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private Provider provider = Provider.LOCAL;
    
    @Column(name = "provider_id", length = 255)
    private String providerId;
    
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;
    
    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;
    
    // Security
    @Column(name = "two_factor_enabled", nullable = false)
    @Builder.Default
    private Boolean twoFactorEnabled = false;
    
    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;
    
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
    
    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Enums
    public enum Role {
        BUYER, SELLER, ADMIN
    }
    
    public enum Status {
        ACTIVE, SUSPENDED, DELETED
    }
    
    public enum Provider {
        LOCAL, GOOGLE, FACEBOOK, GITHUB
    }
    
    public enum Gender {
        MALE, FEMALE, OTHER
    }
}
