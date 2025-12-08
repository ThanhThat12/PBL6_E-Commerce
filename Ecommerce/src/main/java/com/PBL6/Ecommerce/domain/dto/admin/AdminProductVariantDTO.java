package com.PBL6.Ecommerce.domain.dto.admin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO cho variant của sản phẩm
 */
public class AdminProductVariantDTO {
    private Long id;
    private String sku;
    private BigDecimal price;
    private Integer stock;
    private Long sold; // Số lượng đã bán (từ order_items với status COMPLETED)
    private List<AdminProductAttributeDTO> attributes; // Danh sách thuộc tính (Size: S, Màu: Xanh, etc.)
    private List<String> images; // URL ảnh của variant

    public AdminProductVariantDTO() {
        this.attributes = new ArrayList<>();
        this.images = new ArrayList<>();
    }

    public AdminProductVariantDTO(Long id, String sku, BigDecimal price, Integer stock, Long sold) {
        this.id = id;
        this.sku = sku;
        this.price = price;
        this.stock = stock;
        this.sold = sold != null ? sold : 0L;
        this.attributes = new ArrayList<>();
        this.images = new ArrayList<>();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Long getSold() {
        return sold;
    }

    public void setSold(Long sold) {
        this.sold = sold;
    }

    public List<AdminProductAttributeDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AdminProductAttributeDTO> attributes) {
        this.attributes = attributes;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
