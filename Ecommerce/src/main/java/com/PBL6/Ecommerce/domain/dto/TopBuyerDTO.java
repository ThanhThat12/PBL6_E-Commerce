package com.PBL6.Ecommerce.domain.dto;

import java.math.BigDecimal;

public class TopBuyerDTO {
    private Long userId;
    private String username;
    private String email;
    private BigDecimal totalAmount;
    private Long totalCompletedOrders;

    // Constructors
    public TopBuyerDTO() {}

    public TopBuyerDTO(Long userId, String username, String email, BigDecimal totalAmount, Long totalCompletedOrders) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.totalAmount = totalAmount;
        this.totalCompletedOrders = totalCompletedOrders;
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Long getTotalCompletedOrders() { return totalCompletedOrders; }
    public void setTotalCompletedOrders(Long totalCompletedOrders) { this.totalCompletedOrders = totalCompletedOrders; }
}