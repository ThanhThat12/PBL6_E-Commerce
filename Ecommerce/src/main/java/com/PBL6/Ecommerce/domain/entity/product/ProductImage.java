package com.PBL6.Ecommerce.domain.entity.product;

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
           name = "uq_product_variant_image",
           columnNames = {"product_id", "variant_attribute_value", "image_type"}))
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "image_type", nullable = false, length = 20)
    private String imageType = "GALLERY"; // ENUM: 'GALLERY' or 'VARIANT'

    @Column(name = "variant_attribute_value", length = 100)
    private String variantAttributeValue; // e.g., "Red", "Blue"

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "public_id", length = 255)
    private String publicId;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "uploaded_at", updatable = false)
    private java.time.LocalDateTime uploadedAt;

     // Constructors
    public ProductImage() {}

    public ProductImage(String imageUrl, String imageType) {
        this.imageUrl = imageUrl;
        this.imageType = imageType;
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

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public String getVariantAttributeValue() {
        return variantAttributeValue;
    }

    public void setVariantAttributeValue(String variantAttributeValue) {
        this.variantAttributeValue = variantAttributeValue;
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

    public java.time.LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(java.time.LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}