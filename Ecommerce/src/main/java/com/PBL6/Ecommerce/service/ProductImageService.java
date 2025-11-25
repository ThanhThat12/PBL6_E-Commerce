package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.dto.response.ImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for product image management
 * Handles main product images, gallery images, and variant-specific images
 */
public interface ProductImageService {
    
    /**
     * Upload main product image
     * Validates image format, size, and dimensions before uploading to Cloudinary
     * Replaces existing main image if present
     * 
     * @param productId Product ID to upload image for
     * @param file Image file to upload
     * @param userId Authenticated user ID (seller)
     * @return ImageUploadResponse with image URL and metadata
     * @throws com.PBL6.Ecommerce.exception.ProductNotFoundException if product not found
     * @throws com.PBL6.Ecommerce.exception.UnauthorizedProductAccessException if seller doesn't own the product
     * @throws com.PBL6.Ecommerce.exception.ImageValidationException if image validation fails
     * @throws com.PBL6.Ecommerce.exception.ImageUploadException if Cloudinary upload fails
     */
    ImageUploadResponse uploadMainImage(Long productId, MultipartFile file, Long userId);
}
