package com.PBL6.Ecommerce.domain.dto.admin;

import java.time.LocalDateTime;

public class AdminUserDetailDTO {
    // Thông tin chung cho tất cả user
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
    private boolean activated;
    private String facebookId;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    // Thông tin dành cho SELLER
    private String shopName;
    private String shopAddress;
    private String shopDescription;
    private String shopStatus;
    private Integer totalProductsSeller;
    private Integer totalOrdersSeller;
    private Double totalRevenue;

    // Thông tin dành cho BUYER/CUSTOMER
    private Integer totalOrders;
    private Double totalSpent;
    private LocalDateTime lastOrderDate;
    private Integer cartItemsCount;
    
    // Thông tin địa chỉ chính của BUYER/CUSTOMER
    private String primaryAddressLabel;
    private String primaryAddressFullAddress;
    private String primaryAddressProvince;
    private String primaryAddressDistrict;
    private String primaryAddressWard;
    private String primaryAddressContactPhone;

    public AdminUserDetailDTO() {
    }

    // Constructor cho ADMIN
    public AdminUserDetailDTO(Long id, String username, String fullName, String email, String phoneNumber,
                             String role, boolean activated, String facebookId,
                             LocalDateTime createdAt, LocalDateTime lastLogin) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.activated = activated;
        this.facebookId = facebookId;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    // Constructor cho SELLER
    public AdminUserDetailDTO(Long id, String username, String fullName, String email, String phoneNumber,
                             String role, boolean activated, LocalDateTime createdAt,
                             String shopName, String shopAddress, String shopDescription,
                             String shopStatus, Integer totalProductsSeller, 
                             Integer totalOrdersSeller, Double totalRevenue) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.activated = activated;
        this.createdAt = createdAt;
        this.shopName = shopName;
        this.shopAddress = shopAddress;
        this.shopDescription = shopDescription;
        this.shopStatus = shopStatus;
        this.totalProductsSeller = totalProductsSeller;
        this.totalOrdersSeller = totalOrdersSeller;
        this.totalRevenue = totalRevenue;
    }

    // Constructor cho BUYER/CUSTOMER
    public AdminUserDetailDTO(Long id, String username, String fullName, String email, String phoneNumber,
                             String role, boolean activated, LocalDateTime createdAt,
                             Integer totalOrders, Double totalSpent, LocalDateTime lastOrderDate,
                             Integer cartItemsCount,
                             String primaryAddressLabel, String primaryAddressFullAddress,
                             String primaryAddressProvince, String primaryAddressDistrict,
                             String primaryAddressWard, String primaryAddressContactPhone) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.activated = activated;
        this.createdAt = createdAt;
        this.totalOrders = totalOrders;
        this.totalSpent = totalSpent;
        this.lastOrderDate = lastOrderDate;
        this.cartItemsCount = cartItemsCount;
        this.primaryAddressLabel = primaryAddressLabel;
        this.primaryAddressFullAddress = primaryAddressFullAddress;
        this.primaryAddressProvince = primaryAddressProvince;
        this.primaryAddressDistrict = primaryAddressDistrict;
        this.primaryAddressWard = primaryAddressWard;
        this.primaryAddressContactPhone = primaryAddressContactPhone;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(String shopAddress) {
        this.shopAddress = shopAddress;
    }

    public String getShopDescription() {
        return shopDescription;
    }

    public void setShopDescription(String shopDescription) {
        this.shopDescription = shopDescription;
    }

    public String getShopStatus() {
        return shopStatus;
    }

    public void setShopStatus(String shopStatus) {
        this.shopStatus = shopStatus;
    }

    public Integer getTotalProductsSeller() {
        return totalProductsSeller;
    }

    public void setTotalProductsSeller(Integer totalProductsSeller) {
        this.totalProductsSeller = totalProductsSeller;
    }

    public Integer getTotalOrdersSeller() {
        return totalOrdersSeller;
    }

    public void setTotalOrdersSeller(Integer totalOrdersSeller) {
        this.totalOrdersSeller = totalOrdersSeller;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(Double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public LocalDateTime getLastOrderDate() {
        return lastOrderDate;
    }

    public void setLastOrderDate(LocalDateTime lastOrderDate) {
        this.lastOrderDate = lastOrderDate;
    }

    public Integer getCartItemsCount() {
        return cartItemsCount;
    }

    public void setCartItemsCount(Integer cartItemsCount) {
        this.cartItemsCount = cartItemsCount;
    }

    public String getPrimaryAddressLabel() {
        return primaryAddressLabel;
    }

    public void setPrimaryAddressLabel(String primaryAddressLabel) {
        this.primaryAddressLabel = primaryAddressLabel;
    }

    public String getPrimaryAddressFullAddress() {
        return primaryAddressFullAddress;
    }

    public void setPrimaryAddressFullAddress(String primaryAddressFullAddress) {
        this.primaryAddressFullAddress = primaryAddressFullAddress;
    }

    public String getPrimaryAddressProvince() {
        return primaryAddressProvince;
    }

    public void setPrimaryAddressProvince(String primaryAddressProvince) {
        this.primaryAddressProvince = primaryAddressProvince;
    }

    public String getPrimaryAddressDistrict() {
        return primaryAddressDistrict;
    }

    public void setPrimaryAddressDistrict(String primaryAddressDistrict) {
        this.primaryAddressDistrict = primaryAddressDistrict;
    }

    public String getPrimaryAddressWard() {
        return primaryAddressWard;
    }

    public void setPrimaryAddressWard(String primaryAddressWard) {
        this.primaryAddressWard = primaryAddressWard;
    }

    public String getPrimaryAddressContactPhone() {
        return primaryAddressContactPhone;
    }

    public void setPrimaryAddressContactPhone(String primaryAddressContactPhone) {
        this.primaryAddressContactPhone = primaryAddressContactPhone;
    }
}