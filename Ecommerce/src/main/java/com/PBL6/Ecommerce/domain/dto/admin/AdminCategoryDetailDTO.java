package com.PBL6.Ecommerce.domain.dto.admin;

/**
 * DTO cho thông tin category trong product detail
 */
public class AdminCategoryDetailDTO {
    private Long id;
    private String name;

    public AdminCategoryDetailDTO() {
    }

    public AdminCategoryDetailDTO(Long id, String name) {
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
