package com.PBL6.Ecommerce.domain.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public class AdminUpdateSellerDTO {
    
    // Shop fields
    private String shopStatus; // "ACTIVE", "INACTIVE", "PENDING"
    private String shopName;
    private String shopDescription;
    
    // User fields
    private String username;
    
    @Email(message = "Email phải có định dạng @gmail.com")
    @Pattern(regexp = ".*@gmail\\.com$", message = "Email phải có định dạng @gmail.com")
    private String email;
    
    @Pattern(regexp = "^0\\d{9}$", message = "Phone phải bắt đầu bằng 0 và có đúng 10 chữ số")
    private String phone;
    private String fullName;

    // Getters and Setters
    public String getShopStatus() {
        return shopStatus;
    }

    public void setShopStatus(String shopStatus) {
        this.shopStatus = shopStatus;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopDescription() {
        return shopDescription;
    }

    public void setShopDescription(String shopDescription) {
        this.shopDescription = shopDescription;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
