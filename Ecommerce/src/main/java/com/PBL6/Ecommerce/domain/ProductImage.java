package com.PBL6.Ecommerce.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "product_images",
       uniqueConstraints = @UniqueConstraint(
           name = "uq_product_variant_value",
           columnNames = {"product_id", "variant_value_id"}))
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_value_id")
    private ProductVariantValue variantValue;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "public_id", nullable = false, length = 255, unique = true)
    private String publicId;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "variant_value_name", length = 100)
    private String variantValueName;

    // Tạm thời comment out vì database chưa có cột này
    // @Column(name = "created_at", nullable = false, updatable = false)
    // private LocalDateTime createdAt = LocalDateTime.now();

    // @Column(name = "updated_at", nullable = false)
    // private LocalDateTime updatedAt = LocalDateTime.now();

     // Constructors
    public ProductImage() {}

    public ProductImage(String imageUrl, String variantValueName) {
        this.imageUrl = imageUrl;
        this.variantValueName = variantValueName;
        // this.createdAt = LocalDateTime.now();
        // this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVariantValueName() {
        return variantValueName;
    }

    public void setVariantValueName(String variantValueName) {
        this.variantValueName = variantValueName;
    }

    public ProductVariant getVariant() {
        return variant;
    }

    public void setVariant(ProductVariant variant) {
        this.variant = variant;
    }

    public ProductVariantValue getVariantValue() {
        return variantValue;
    }

    public void setVariantValue(ProductVariantValue variantValue) {
        this.variantValue = variantValue;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    // public LocalDateTime getCreatedAt() {
    //     return createdAt;
    // }

    // public void setCreatedAt(LocalDateTime createdAt) {
    //     this.createdAt = createdAt;
    // }

    // public LocalDateTime getUpdatedAt() {
    //     return updatedAt;
    // }

    // public void setUpdatedAt(LocalDateTime updatedAt) {
    //     this.updatedAt = updatedAt;
    // }
}