package com.PBL6.Ecommerce.domain.dto.admin;

import java.time.LocalDate;
import jakarta.validation.constraints.*;

public class AdminUpdateVoucherDTO {
    @NotBlank(message = "Voucher code is required")
    @Pattern(regexp = "^[A-Z0-9]{1,10}$", message = "Code must contain only uppercase letters and numbers, max 10 characters")
    private String code;
    
    private String description; // Có thể null
    
    @NotNull(message = "Discount amount is required")
    @Min(value = 0, message = "Discount amount must be at least 0")
    private Integer discountAmount;
    
    @Min(value = 0, message = "Minimum order value must be at least 0")
    private Integer minOrderValue;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    private String status; // ACTIVE, INACTIVE, EXPIRED

    // Constructors
    public AdminUpdateVoucherDTO() {}

    public AdminUpdateVoucherDTO(String code, String description, 
                                 Integer discountAmount, Integer minOrderValue,
                                 Integer quantity, LocalDate startDate, 
                                 LocalDate endDate, String status) {
        this.code = code;
        this.description = description;
        this.discountAmount = discountAmount;
        this.minOrderValue = minOrderValue;
        this.quantity = quantity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Integer discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Integer getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(Integer minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
