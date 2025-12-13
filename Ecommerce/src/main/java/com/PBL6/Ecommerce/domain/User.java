package com.PBL6.Ecommerce.domain;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;


@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, unique = true, nullable = false)
    private String username;

    @Column(length = 100, unique = true, nullable = true)
    private String email;

    @Column(length = 100, unique = true, nullable = true)
    private String phoneNumber;

    @Column(length = 60, nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean activated = false;

    @Column(nullable = false)
    private Role role;

    @Column(length = 100, unique = true)
    private String facebookId;

    @Column(length = 100, unique = true)
    private String googleId;

    @Column(length = 100)
    private String fullName;

    @Column(length = 500)
    private String avatarUrl;

    @Column(name = "avatar_public_id", length = 255)
    private String avatarPublicId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Thêm quan hệ với Shop
    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Shop shop;

     // Thêm quan hệ với Address (một user có nhiều địa chỉ)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses;

    public List<Address> getAddresses() { return addresses; }
    public void setAddresses(List<Address> addresses) { this.addresses = addresses; }
    
public void setActivated(boolean activated) {
    this.activated = activated;
}

public void setUsername(String username) {
    this.username = username;
}

public void setEmail(String email) {
    this.email = email;
}

public void setPassword(String password) {
    this.password = password;
}
public String getUsername() {
    return username;
}
public String getEmail() {
    return email;
}
public String getPassword() {
    return password;
}
public boolean isActivated() {
    return activated;
}
public Long getId() {
    return id;
}
public void setId(Long id) {
    this.id = id;
}
public Role getRole() {
    return role;
}
public void setRole(Role role) {
    this.role = role;
}
public String getFacebookId() {
    return facebookId;
}
public void setFacebookId(String facebookId) {
    this.facebookId = facebookId;
}
public String getGoogleId() {
    return googleId;
}
public void setGoogleId(String googleId) {
    this.googleId = googleId;
}
public String getPhoneNumber() {
    return phoneNumber;
}
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarPublicId() {
        return avatarPublicId;
    }

    public void setAvatarPublicId(String avatarPublicId) {
        this.avatarPublicId = avatarPublicId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Shop getShop() { return shop; }
    public void setShop(Shop shop) { this.shop = shop; }

}