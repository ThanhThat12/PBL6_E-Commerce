package com.PBL6.Ecommerce.domain.dto;

import java.time.Instant;

/**
 * Generic API response wrapper for seller registration operations
 */
public class SellerRegistrationResponseDTO {
    
    private boolean success;
    private String message;
    private Long shopId;
    private String shopName;
    private String status;
    private Instant timestamp;

    // Constructors
    public SellerRegistrationResponseDTO() {
        this.timestamp = Instant.now();
    }

    public SellerRegistrationResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = Instant.now();
    }

    // Static factory methods
    public static SellerRegistrationResponseDTO success(String message, Long shopId, String shopName, String status) {
        SellerRegistrationResponseDTO response = new SellerRegistrationResponseDTO(true, message);
        response.setShopId(shopId);
        response.setShopName(shopName);
        response.setStatus(status);
        return response;
    }

    public static SellerRegistrationResponseDTO error(String message) {
        return new SellerRegistrationResponseDTO(false, message);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
