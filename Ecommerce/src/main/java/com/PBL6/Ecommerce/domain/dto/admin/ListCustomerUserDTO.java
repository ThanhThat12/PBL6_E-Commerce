package com.PBL6.Ecommerce.domain.dto.admin;

import java.time.LocalDateTime;

public class ListCustomerUserDTO {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String phoneNumber;
    private boolean activated;
    private LocalDateTime registeredAt;
    private LocalDateTime lastOrderDate;
    private Integer totalOrders;
    private Double totalSpent;

    public ListCustomerUserDTO(Long id, String fullName, String username, String email, String phoneNumber,
                          boolean activated, LocalDateTime registeredAt, LocalDateTime lastOrderDate,
                          Integer totalOrders, Double totalSpent) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.activated = activated;
        this.registeredAt = registeredAt;
        this.lastOrderDate = lastOrderDate;
        this.totalOrders = totalOrders;
        this.totalSpent = totalSpent;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public boolean isActivated() { return activated; }
    public void setActivated(boolean activated) { this.activated = activated; }
    
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
    
    public LocalDateTime getLastOrderDate() { return lastOrderDate; }
    public void setLastOrderDate(LocalDateTime lastOrderDate) { this.lastOrderDate = lastOrderDate; }
    
    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
    
    public Double getTotalSpent() { return totalSpent; }
    public void setTotalSpent(Double totalSpent) { this.totalSpent = totalSpent; }
}