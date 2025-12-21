package com.PBL6.Ecommerce.domain.dto;

import java.util.List;

import jakarta.validation.Valid;

/**
 * ProductUpdateDTO for updating existing products
 * RESTRICTED UPDATE - Only allows updating:
 * 1. SKU for variants (via variants list)
 * 2. Stock for variants (via variants list)
 * 
 * Images are handled separately via ImageUploadService endpoints:
 * - POST /api/products/{id}/images/main - Upload main image
 * - POST /api/products/{id}/images/gallery - Upload gallery images (max 5)
 * - POST /api/products/{id}/images/variant - Upload variant images
 * 
 * Other product fields (name, description, price, category, etc.) are immutable after creation
 */
public class ProductUpdateDTO {
    
    // Variant SKU and Stock updates only
    @Valid
    private List<ProductVariantDTO> variants;
    
    // Default constructor
    public ProductUpdateDTO() {}

    // Getters and Setters
    public List<ProductVariantDTO> getVariants() { return variants; }
    public void setVariants(List<ProductVariantDTO> variants) { this.variants = variants; }
}