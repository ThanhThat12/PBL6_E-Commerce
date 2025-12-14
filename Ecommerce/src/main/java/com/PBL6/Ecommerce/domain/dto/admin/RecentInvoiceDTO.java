package com.PBL6.Ecommerce.domain.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RecentInvoiceDTO {
    private Long id;
    private String name; // Shop name or buyer name
    private String avatar; // Shop logo or user avatar
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private Long orderId;

    // Constructors
    public RecentInvoiceDTO() {
    }

    public RecentInvoiceDTO(Long id, String name, String avatar, BigDecimal amount, LocalDateTime createdAt, Long orderId) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.amount = amount;
        this.createdAt = createdAt;
        this.orderId = orderId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
