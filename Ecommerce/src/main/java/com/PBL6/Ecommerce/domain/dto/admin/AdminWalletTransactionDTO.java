package com.PBL6.Ecommerce.domain.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminWalletTransactionDTO {
    
    private Long id;
    private String type;
    private String description;
    private BigDecimal amount;
    private Long orderId;
    private LocalDateTime createdAt;
    
    // Constructors
    public AdminWalletTransactionDTO() {
    }
    
    public AdminWalletTransactionDTO(Long id, String type, String description, 
                                     BigDecimal amount, Long orderId, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.orderId = orderId;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
