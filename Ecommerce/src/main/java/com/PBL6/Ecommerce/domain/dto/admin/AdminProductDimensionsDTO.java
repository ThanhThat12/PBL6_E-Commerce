package com.PBL6.Ecommerce.domain.dto.admin;

/**
 * DTO cho kích thước sản phẩm
 */
public class AdminProductDimensionsDTO {
    private Integer heightCm;
    private Integer lengthCm;
    private Integer widthCm;

    public AdminProductDimensionsDTO() {
    }

    public AdminProductDimensionsDTO(Integer heightCm, Integer lengthCm, Integer widthCm) {
        this.heightCm = heightCm;
        this.lengthCm = lengthCm;
        this.widthCm = widthCm;
    }

    // Getters and Setters
    public Integer getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Integer heightCm) {
        this.heightCm = heightCm;
    }

    public Integer getLengthCm() {
        return lengthCm;
    }

    public void setLengthCm(Integer lengthCm) {
        this.lengthCm = lengthCm;
    }

    public Integer getWidthCm() {
        return widthCm;
    }

    public void setWidthCm(Integer widthCm) {
        this.widthCm = widthCm;
    }
}
