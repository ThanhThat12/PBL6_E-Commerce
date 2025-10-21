package com.PBL6.Ecommerce.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderDTO {
    private Long id;
    private LocalDateTime createdAt;
    private String method;
    private String status;
    private BigDecimal totalAmount;
    private Long userId;

    // Constructors
    public OrderDTO() {}

    public OrderDTO(Long id, LocalDateTime createdAt, String method, String status, BigDecimal totalAmount, Long userId) {
        this.id = id;
        this.createdAt = createdAt;
        this.method = method;
        this.status = status;
        this.totalAmount = totalAmount;
        this.userId = userId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
