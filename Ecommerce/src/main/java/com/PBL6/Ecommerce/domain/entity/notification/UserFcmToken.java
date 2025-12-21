package com.PBL6.Ecommerce.domain.entity.notification;

import com.PBL6.Ecommerce.domain.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity mapping for user_fcm_tokens table
 * Stores FCM device tokens for push notifications
 */
@Entity
@Table(name = "user_fcm_tokens", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "device_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFcmToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "fcm_token", nullable = false, length = 500)
    private String fcmToken;
    
    @Column(name = "device_id", nullable = false, length = 100)
    private String deviceId; // Unique device identifier
    
    @Column(name = "device_type", length = 50)
    private String deviceType; // android, ios, web
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
