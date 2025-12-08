package com.PBL6.Ecommerce.domain.dto.admin;

/**
 * DTO cho ảnh sản phẩm
 */
public class AdminProductImageDTO {
    private Long id;
    private String url;
    private Boolean isMain;

    public AdminProductImageDTO() {
    }

    public AdminProductImageDTO(Long id, String url, Boolean isMain) {
        this.id = id;
        this.url = url;
        this.isMain = isMain;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getIsMain() {
        return isMain;
    }

    public void setIsMain(Boolean isMain) {
        this.isMain = isMain;
    }
}
