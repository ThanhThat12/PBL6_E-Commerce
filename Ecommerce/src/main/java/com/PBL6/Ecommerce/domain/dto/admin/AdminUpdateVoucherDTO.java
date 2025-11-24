package com.PBL6.Ecommerce.domain.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminUpdateVoucherDTO {
    private String code;
    private String description;
    private BigDecimal discountAmount;
    private BigDecimal minOrderValue;
    private Integer quantity;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private Long shopId; // Optional: null for global vouchers, specific ID for shop vouchers

    // Constructors
    public AdminUpdateVoucherDTO() {}

    public AdminUpdateVoucherDTO(String code, String description, BigDecimal discountAmount,
                                BigDecimal minOrderValue, Integer quantity,
                                LocalDateTime startDate, LocalDateTime endDate, String status, Long shopId) {
        this.code = code;
        this.description = description;
        this.discountAmount = discountAmount;
        this.minOrderValue = minOrderValue;
        this.quantity = quantity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.shopId = shopId;
    }

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getMinOrderValue() { return minOrderValue; }
    public void setMinOrderValue(BigDecimal minOrderValue) { this.minOrderValue = minOrderValue; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
}