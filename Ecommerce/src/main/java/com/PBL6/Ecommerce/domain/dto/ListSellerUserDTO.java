package com.PBL6.Ecommerce.domain.dto;

import java.time.LocalDateTime;

public class ListSellerUserDTO {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String shopName;
    private String shopAddress;
    private boolean activated;
    private LocalDateTime registeredAt;
    private Integer totalProducts;
    private Integer totalOrders;

    public ListSellerUserDTO(Long id, String username, String email, String phoneNumber,
                        String shopName, String shopAddress, boolean activated,
                        LocalDateTime registeredAt, Integer totalProducts, Integer totalOrders) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.shopName = shopName;
        this.shopAddress = shopAddress;
        this.activated = activated;
        this.registeredAt = registeredAt;
        this.totalProducts = totalProducts;
        this.totalOrders = totalOrders;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    
    public String getShopAddress() { return shopAddress; }
    public void setShopAddress(String shopAddress) { this.shopAddress = shopAddress; }
    
    public boolean isActivated() { return activated; }
    public void setActivated(boolean activated) { this.activated = activated; }
    
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
    
    public Integer getTotalProducts() { return totalProducts; }
    public void setTotalProducts(Integer totalProducts) { this.totalProducts = totalProducts; }
    
    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
}