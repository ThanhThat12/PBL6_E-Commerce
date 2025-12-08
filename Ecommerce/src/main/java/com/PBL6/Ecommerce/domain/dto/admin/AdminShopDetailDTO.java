package com.PBL6.Ecommerce.domain.dto.admin;

/**
 * DTO cho thông tin shop trong product detail
 */
public class AdminShopDetailDTO {
    private Long id;
    private String name;

    public AdminShopDetailDTO() {
    }

    public AdminShopDetailDTO(Long id, String name) {
        this.id = id;
        this.name = name;
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
}
