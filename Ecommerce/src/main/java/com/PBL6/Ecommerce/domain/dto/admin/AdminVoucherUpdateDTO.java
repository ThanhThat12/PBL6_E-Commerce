package com.PBL6.Ecommerce.domain.dto.admin;

import com.PBL6.Ecommerce.domain.entity.voucher.Vouchers;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for updating voucher (Admin only)
 * Note: applicableType cannot be changed after creation
 */
public class AdminVoucherUpdateDTO {

    @NotBlank(message = "Voucher code is required")
    @Size(min = 3, max = 50, message = "Voucher code must be between 3 and 50 characters")
    private String code;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Discount type is required")
    private Vouchers.DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.01", message = "Max discount amount must be greater than 0")
    private BigDecimal maxDiscountAmount;

    @NotNull(message = "Minimum order value is required")
    @DecimalMin(value = "0", message = "Minimum order value cannot be negative")
    private BigDecimal minOrderValue;

    @NotNull(message = "Usage limit is required")
    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    // Status is auto-calculated based on dates - not required from client
    private Vouchers.Status status;

    // Constructor
    public AdminVoucherUpdateDTO() {
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code != null ? code.toUpperCase() : null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Vouchers.DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(Vouchers.DiscountType discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public BigDecimal getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
    }

    public BigDecimal getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(BigDecimal minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public Integer getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(Integer usageLimit) {
        this.usageLimit = usageLimit;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Vouchers.Status getStatus() {
        return status;
    }

    public void setStatus(Vouchers.Status status) {
        this.status = status;
    }
}
