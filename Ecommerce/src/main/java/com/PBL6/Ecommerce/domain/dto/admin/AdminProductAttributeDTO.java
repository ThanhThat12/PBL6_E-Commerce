package com.PBL6.Ecommerce.domain.dto.admin;

/**
 * DTO cho thuộc tính của variant sản phẩm (ví dụ: Size: S, Màu: Xanh-đỏ)
 */
public class AdminProductAttributeDTO {
    private String name;  // Tên thuộc tính (Size, Màu, etc.)
    private String value; // Giá trị (S, M, L, Xanh, Đỏ, etc.)

    public AdminProductAttributeDTO() {
    }

    public AdminProductAttributeDTO(String name, String value) {
        this.name = name;
        this.value = value;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
