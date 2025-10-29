package com.PBL6.Ecommerce.domain.dto;

import java.time.LocalDateTime;

/**
 * DTO để hiển thị thông tin shop
 * Chỉ chứa: name, address, description, status, created_at
 */
public class ShopDTO {
    private Long id;
    private String name;
    private String address;
    private String description;
    private String status;
    private LocalDateTime createdAt;

    public ShopDTO() {
    }

    public ShopDTO(Long id, String name, String address, String description, String status, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
