package com.PBL6.Ecommerce.domain.dto.admin;

public class AdminVoucherListDTO {
    private Long id;
    private String code;
    private Integer discountAmount;
    private Integer minOrderValue;
    private Integer quantity;
    private Long used; // Số lượng đã sử dụng
    private String status;

    // Constructors
    public AdminVoucherListDTO() {}

    public AdminVoucherListDTO(Long id, String code, Integer discountAmount,
                               Integer minOrderValue, Integer quantity,
                               Long used, String status) {
        this.id = id;
        this.code = code;
        this.discountAmount = discountAmount;
        this.minOrderValue = minOrderValue;
        this.quantity = quantity;
        this.used = used;
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

    public Long getUsed() {
        return used;
    }

    public void setUsed(Long used) {
        this.used = used;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
