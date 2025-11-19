package com.PBL6.Ecommerce.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.PBL6.Ecommerce.dto.request.ImageReorderRequest;
import com.PBL6.Ecommerce.dto.response.ImageDeleteResponse;
import com.PBL6.Ecommerce.dto.response.ImageUploadResponse;
import com.PBL6.Ecommerce.dto.response.ProductImageResponse;

/**
 * Service interface for managing image uploads, transformations, and deletions
 * across multiple entity types (products, users, shops, reviews).
 * 
 * This interface abstracts the business logic for Cloudinary integration
 * and image management operations.
 */
public interface ImageService {

    // ========== PRODUCT MAIN IMAGE ==========

    /**
     * Upload or replace the main image for a product.
     * If a main image already exists, the old image is deleted from Cloudinary.
     * 
     * @param productId The ID of the product
     * @param file The image file to upload (MultipartFile)
     * @param userId The ID of the authenticated user (for ownership validation)
     * @return ImageUploadResponse with URL, public_id, and transformation URLs
     * @throws ImageValidationException if file validation fails
     * @throws ImageUploadException if upload to Cloudinary fails
     * @throws com.PBL6.Ecommerce.exception.ProductNotFoundException if product not found
     * @throws SecurityException if user does not own the product
     */
    ImageUploadResponse uploadProductMainImage(Long productId, MultipartFile file, Long userId);

    /**
     * Delete the main image for a product.
     * Sets main_image and main_image_public_id to NULL in the database.
     * 
     * @param productId The ID of the product
     * @param userId The ID of the authenticated user (for ownership validation)
     * @return ImageDeleteResponse with success status and deleted public_id
     * @throws ImageNotFoundException if product has no main image
     * @throws SecurityException if user does not own the product
     */
    ImageDeleteResponse deleteProductMainImage(Long productId, Long userId);

    // ========== PRODUCT GALLERY IMAGES ==========

    /**
     * Upload multiple images to product gallery (up to 10 images per product).
     * Supports variant-specific images via variantId parameter.
     * 
     * @param productId The ID of the product
     * @param files List of image files to upload (1-10 files)
     * @param userId The ID of the authenticated user (for ownership validation)
     * @param variantId Optional variant ID to associate images with specific variant
     * @return List of ImageUploadResponse objects (one per uploaded image)
     * @throws ImageValidationException if file validation fails or exceeds 10 images
     * @throws ImageUploadException if upload to Cloudinary fails
     * @throws SecurityException if user does not own the product
     */
    List<ImageUploadResponse> uploadProductGalleryImages(Long productId, List<MultipartFile> files, Long userId, Long variantId);

    /**
     * Retrieve all gallery images for a product, ordered by display_order.
     * Optionally filter by variantId if provided.
     * 
     * @param productId The ID of the product
     * @param variantId Optional variant ID to filter images (null for main gallery)
     * @return List of ProductImageResponse objects with transformation URLs
     */
    List<ProductImageResponse> getProductGalleryImages(Long productId, Long variantId);

    /**
     * Reorder gallery images for a product by updating display_order.
     * Accepts an ordered list of image IDs and assigns display_order sequentially (0, 1, 2, ...).
     * 
     * @param productId The ID of the product
     * @param request ImageReorderRequest containing ordered list of image IDs
     * @param userId The ID of the authenticated user (for ownership validation)
     * @throws ImageNotFoundException if any image ID is invalid
     * @throws SecurityException if user does not own the product
     */
    void reorderProductGalleryImages(Long productId, ImageReorderRequest request, Long userId);

    /**
     * Delete a single gallery image by its ID.
     * Removes the image from Cloudinary and the database.
     * 
     * @param productId The ID of the product (for validation)
     * @param imageId The ID of the gallery image to delete
     * @param userId The ID of the authenticated user (for ownership validation)
     * @return ImageDeleteResponse with success status and deleted public_id
     * @throws ImageNotFoundException if image not found
     * @throws SecurityException if user does not own the product
     */
    ImageDeleteResponse deleteProductGalleryImage(Long productId, Long imageId, Long userId);

    /**
     * Batch upload images for multiple Group 1 variants in a single API call.
     * Resolves Group 1 variant value names (e.g., "Red", "Blue") to variant IDs.
     * Associates images only with Group 1 values - all variants with same Group 1 value share images.
     * 
     * Example: Product has variants (Color, Size) where Color is Group 1.
     * If you upload images for "Red", all Red variants (Red-Small, Red-Medium, Red-Large) share those images.
     * 
     * @param productId The ID of the product
     * @param files Array of image files (referenced by indices in variantMappings)
     * @param variantMappings Map from Group 1 value name to file indices (e.g., {"Red": [0,1], "Blue": [2,3]})
     * @param userId The ID of the authenticated user (for ownership validation)
     * @return Map from variant value name to list of ImageUploadResponse objects
     * @throws ImageValidationException if validation fails or variant not found
     * @throws ImageUploadException if upload to Cloudinary fails
     * @throws SecurityException if user does not own the product
     */
    java.util.Map<String, List<ImageUploadResponse>> uploadBatchVariantImages(
            Long productId, 
            List<MultipartFile> files,
            java.util.Map<String, List<Integer>> variantMappings,
            Long userId);

    // ========== USER AVATAR ==========

    /**
     * Upload or replace user avatar with face detection transformation (300x300).
     * If an avatar already exists, the old avatar is deleted from Cloudinary.
     * 
     * @param userId The ID of the user
     * @param file The avatar image file to upload (MultipartFile)
     * @return ImageUploadResponse with URL, public_id, and transformation URLs
     * @throws ImageValidationException if file validation fails (min 100x100, max 4096x4096)
     * @throws ImageUploadException if upload to Cloudinary fails
     * @throws SecurityException if user ID mismatch
     */
    ImageUploadResponse uploadUserAvatar(Long userId, MultipartFile file);

    /**
     * Delete user avatar and set placeholder.
     * Sets avatar_url and avatar_public_id to NULL in the database.
     * 
     * @param userId The ID of the user
     * @return ImageDeleteResponse with success status and deleted public_id
     * @throws ImageNotFoundException if user has no avatar
     * @throws SecurityException if user ID mismatch
     */
    ImageDeleteResponse deleteUserAvatar(Long userId);

    // ========== SHOP BRANDING IMAGES ==========

    /**
     * Upload or replace shop logo with transparency support (400x400).
     * If a logo already exists, the old logo is deleted from Cloudinary.
     * 
     * @param shopId The ID of the shop
     * @param file The logo image file to upload (MultipartFile)
     * @param userId The ID of the authenticated user (for ownership validation)
     * @return ImageUploadResponse with URL, public_id, and transformation URLs
     * @throws ImageValidationException if file validation fails
     * @throws ImageUploadException if upload to Cloudinary fails
     * @throws com.PBL6.Ecommerce.exception.ShopNotFoundException if shop not found
     * @throws SecurityException if user does not own the shop
     */
    ImageUploadResponse uploadShopLogo(Long shopId, MultipartFile file, Long userId);

    /**
     * Upload or replace shop banner with crop fill transformation (1200x400).
     * If a banner already exists, the old banner is deleted from Cloudinary.
     * 
     * @param shopId The ID of the shop
     * @param file The banner image file to upload (MultipartFile)
     * @param userId The ID of the authenticated user (for ownership validation)
     * @return ImageUploadResponse with URL, public_id, and transformation URLs
     * @throws ImageValidationException if file validation fails
     * @throws ImageUploadException if upload to Cloudinary fails
     * @throws com.PBL6.Ecommerce.exception.ShopNotFoundException if shop not found
     * @throws SecurityException if user does not own the shop
     */
    ImageUploadResponse uploadShopBanner(Long shopId, MultipartFile file, Long userId);

    /**
     * Delete shop logo.
     * Sets logo_url and logo_public_id to NULL in the database.
     * 
     * @param shopId The ID of the shop
     * @param userId The ID of the authenticated user (for ownership validation)
     * @return ImageDeleteResponse with success status and deleted public_id
     * @throws ImageNotFoundException if shop has no logo
     * @throws SecurityException if user does not own the shop
     */
    ImageDeleteResponse deleteShopLogo(Long shopId, Long userId);

    /**
     * Delete shop banner.
     * Sets banner_url and banner_public_id to NULL in the database.
     * 
     * @param shopId The ID of the shop
     * @param userId The ID of the authenticated user (for ownership validation)
     * @return ImageDeleteResponse with success status and deleted public_id
     * @throws ImageNotFoundException if shop has no banner
     * @throws SecurityException if user does not own the shop
     */
    ImageDeleteResponse deleteShopBanner(Long shopId, Long userId);

    // ========== REVIEW IMAGES ==========

    /**
     * Upload images to a product review (up to 5 images).
     * Images are stored as JSON array in the images column.
     * Each image object contains: {"url": "...", "publicId": "..."}.
     * 
     * @param reviewId The ID of the review
     * @param files List of image files to upload (1-5 files)
     * @param userId The ID of the authenticated user (for ownership validation)
     * @return List of ImageUploadResponse objects (one per uploaded image)
     * @throws ImageValidationException if file validation fails or exceeds 5 images
     * @throws ImageUploadException if upload to Cloudinary fails
     * @throws SecurityException if user does not own the review
     */
    List<ImageUploadResponse> uploadReviewImages(Long reviewId, List<MultipartFile> files, Long userId);

    /**
     * Delete a single image from a review by its index in the JSON array.
     * Updates the images JSON array and images_count.
     * 
     * @param reviewId The ID of the review
     * @param imageIndex The index of the image to delete (0-based)
     * @param userId The ID of the authenticated user (for ownership validation)
     * @return ImageDeleteResponse with success status and deleted public_id
     * @throws ImageNotFoundException if review has no images or index out of bounds
     * @throws SecurityException if user does not own the review
     */
    ImageDeleteResponse deleteReviewImage(Long reviewId, Integer imageIndex, Long userId);
}

