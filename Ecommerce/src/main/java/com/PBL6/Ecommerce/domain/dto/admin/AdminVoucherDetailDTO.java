package com.PBL6.Ecommerce.domain.dto.admin;

import java.time.LocalDate;

public class AdminVoucherDetailDTO {
    private Long id;
    private String code;
    private String description;
    private String shopName; // Tên shop (nếu có)
    private Integer discountAmount;
    private Integer minOrderValue;
    private Integer quantity;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;

    // Constructors
    public AdminVoucherDetailDTO() {}

    public AdminVoucherDetailDTO(Long id, String code, String description, 
                                 String shopName, Integer discountAmount,
                                 Integer minOrderValue, Integer quantity,
                                 LocalDate startDate, LocalDate endDate,
                                 String status) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.shopName = shopName;
        this.discountAmount = discountAmount;
        this.minOrderValue = minOrderValue;
        this.quantity = quantity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
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
