package com.PBL6.Ecommerce.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.dto.request.ImageReorderRequest;
import com.PBL6.Ecommerce.dto.response.ImageDeleteResponse;
import com.PBL6.Ecommerce.dto.response.ImageUploadResponse;
import com.PBL6.Ecommerce.dto.response.ProductImageResponse;
import com.PBL6.Ecommerce.service.ImageService;
import com.PBL6.Ecommerce.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for managing product images (main image and gallery).
 * 
 * Endpoints:
 * - POST   /api/products/{id}/images/main           - Upload/replace main image
 * - DELETE /api/products/{id}/images/main           - Delete main image
 * - POST   /api/products/{id}/images/gallery        - Upload gallery images (batch)
 * - GET    /api/products/{id}/images/gallery        - Retrieve gallery images
 * - PUT    /api/products/{id}/images/gallery/reorder - Reorder gallery images
 * - DELETE /api/products/{id}/images/gallery/{imageId} - Delete gallery image
 * 
 * Security:
 * - All endpoints require SELLER or ADMIN role
 * - Ownership validation enforced in service layer
 * - Rate limiting applied to uploads (10/min, 50/hr per user)
 */
@RestController
@RequestMapping("/api/products/{productId}/images")
@RequiredArgsConstructor
@Slf4j
public class ProductImageController {

    private final ImageService imageService;
    private final UserService userService;

    // ========== MAIN IMAGE ENDPOINTS ==========

    /**
     * Upload or replace the main image for a product.
     * 
     * @param productId Product ID
     * @param file Image file (JPG, JPEG, PNG, WEBP, max 5MB)
     * @param authentication Spring Security authentication
     * @return ImageUploadResponse with URL and transformations
     */
    @PostMapping(value = "/main", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<ImageUploadResponse>> uploadMainImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        log.info("POST /api/products/{}/images/main - Upload main image", productId);
        
        Long userId = extractUserId(authentication);
        ImageUploadResponse response = imageService.uploadProductMainImage(productId, file, userId);
        
        return ResponseDTO.success(response, "Main image uploaded successfully");
    }

    /**
     * Delete the main image for a product.
     * 
     * @param productId Product ID
     * @param authentication Spring Security authentication
     * @return ImageDeleteResponse with success status
     */
    @DeleteMapping("/main")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<ImageDeleteResponse>> deleteMainImage(
            @PathVariable Long productId,
            Authentication authentication) {
        
        log.info("DELETE /api/products/{}/images/main - Delete main image", productId);
        
        Long userId = extractUserId(authentication);
        ImageDeleteResponse response = imageService.deleteProductMainImage(productId, userId);
        
        return ResponseDTO.success(response, "Main image deleted successfully");
    }

    // ========== GALLERY IMAGE ENDPOINTS ==========

    /**
     * Upload multiple gallery images for a product (batch upload).
     * 
     * @param productId Product ID
     * @param files List of image files (1-10 files, JPG/PNG/WEBP, max 5MB each)
     * @param authentication Spring Security authentication
     * @return List of ImageUploadResponse (one per uploaded image)
     */
    @PostMapping(value = "/gallery", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<List<ImageUploadResponse>>> uploadGalleryImages(
            @PathVariable Long productId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(required = false) Long variantId,
            Authentication authentication) {
        
        log.info("POST /api/products/{}/images/gallery - Upload {} gallery images (variant: {})", 
                 productId, files.size(), variantId);
        
        Long userId = extractUserId(authentication);
        List<ImageUploadResponse> responses = imageService.uploadProductGalleryImages(productId, files, userId, variantId);
        
        return ResponseDTO.success(responses, String.format("Successfully uploaded %d gallery images", responses.size()));
    }

    /**
     * Retrieve all gallery images for a product (ordered by display_order).
     * 
     * @param productId Product ID
     * @param variantId Optional variant ID filter
     * @return List of ProductImageResponse with transformation URLs
     */
    @GetMapping("/gallery")
    public ResponseEntity<ResponseDTO<List<ProductImageResponse>>> getGalleryImages(
            @PathVariable Long productId,
            @RequestParam(required = false) Long variantId) {
        
        log.info("GET /api/products/{}/images/gallery - Retrieve gallery images (variantId: {})", 
                 productId, variantId);
        
        List<ProductImageResponse> images = imageService.getProductGalleryImages(productId, variantId);
        
        return ResponseDTO.success(images, String.format("Retrieved %d gallery images", images.size()));
    }

    /**
     * Reorder gallery images by updating display_order.
     * 
     * @param productId Product ID
     * @param request ImageReorderRequest with ordered list of image IDs
     * @param authentication Spring Security authentication
     * @return Success response
     */
    @PutMapping("/gallery/reorder")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<Void>> reorderGalleryImages(
            @PathVariable Long productId,
            @Valid @RequestBody ImageReorderRequest request,
            Authentication authentication) {
        
        log.info("PUT /api/products/{}/images/gallery/reorder - Reorder {} images", 
                 productId, request.getImageOrders().size());
        
        Long userId = extractUserId(authentication);
        imageService.reorderProductGalleryImages(productId, request, userId);
        
        return ResponseDTO.success(null, "Gallery images reordered successfully");
    }

    /**
     * Delete a single gallery image by its ID.
     * 
     * @param productId Product ID (for validation)
     * @param imageId Gallery image ID to delete
     * @param authentication Spring Security authentication
     * @return ImageDeleteResponse with success status
     */
    @DeleteMapping("/gallery/{imageId}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<ImageDeleteResponse>> deleteGalleryImage(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            Authentication authentication) {
        
        log.info("DELETE /api/products/{}/images/gallery/{} - Delete gallery image", productId, imageId);
        
        Long userId = extractUserId(authentication);
        ImageDeleteResponse response = imageService.deleteProductGalleryImage(productId, imageId, userId);
        
        return ResponseDTO.success(response, "Gallery image deleted successfully");
    }

    // ========== VARIANT-SPECIFIC IMAGES (Phase 5) ==========

    /**
     * Upload a variant-specific image for a primary attribute value.
     * Example: Upload image for Color="Red" where Color is the primary attribute.
     * If image already exists for this attribute value, it will be replaced.
     * 
     * @param productId Product ID
     * @param file Image file (JPG, JPEG, PNG, WEBP, max 5MB)
     * @param attributeValue Attribute value (e.g., "Red", "Blue", "Green")
     * @param authentication Spring Security authentication
     * @return VariantImageResponse with image details
     */
    @PostMapping(value = "/variant", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<com.PBL6.Ecommerce.dto.response.VariantImageResponse>> uploadVariantImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("attributeValue") String attributeValue,
            Authentication authentication) {
        
        log.info("POST /api/products/{}/images/variant - Upload variant image for '{}'", 
                 productId, attributeValue);
        
        Long userId = extractUserId(authentication);
        com.PBL6.Ecommerce.dto.response.VariantImageResponse response = 
            imageService.uploadVariantImage(productId, file, attributeValue, userId);
        
        return ResponseDTO.success(response, 
            String.format("Variant image uploaded successfully for '%s'", attributeValue));
    }

    /**
     * Delete a variant-specific image for a primary attribute value.
     * 
     * @param productId Product ID
     * @param attributeValue Attribute value (e.g., "Red", "Blue")
     * @param authentication Spring Security authentication
     * @return Success response
     */
    @DeleteMapping("/variant")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<Void>> deleteVariantImage(
            @PathVariable Long productId,
            @RequestParam("attributeValue") String attributeValue,
            Authentication authentication) {
        
        log.info("DELETE /api/products/{}/images/variant - Delete variant image for '{}'", 
                 productId, attributeValue);
        
        Long userId = extractUserId(authentication);
        imageService.deleteVariantImage(productId, attributeValue, userId);
        
        return ResponseDTO.success(null, 
            String.format("Variant image deleted successfully for '%s'", attributeValue));
    }

    /**
     * Get all product images including main, gallery, and variant-specific images.
     * This is a public endpoint for buyers to view all product images.
     * 
     * @param productId Product ID
     * @return ProductImagesResponse with all image types
     */
    @GetMapping("")
    public ResponseEntity<ResponseDTO<com.PBL6.Ecommerce.dto.response.ProductImagesResponse>> getAllProductImages(
            @PathVariable Long productId) {
        
        log.info("GET /api/products/{}/images - Retrieve all product images", productId);
        
        com.PBL6.Ecommerce.dto.response.ProductImagesResponse response = 
            imageService.getProductImages(productId);
        
        return ResponseDTO.success(response, "Product images retrieved successfully");
    }

    // ========== BATCH VARIANT IMAGE UPLOAD ==========

    /**
     * Batch upload images for multiple Group 1 variants in a single API call.
     * Accepts multipart form-data with files[] and variantMappings JSON field.
     * 
     * Example variantMappings: {"Red": [0, 1], "Blue": [2, 3], "Green": [4, 5]}
     * - Key: Group 1 variant value name (e.g., "Red", "Blue")
     * - Value: Array of file indices referencing files[] array
     * 
     * @param productId Product ID
     * @param files Array of image files (max 10 total)
     * @param variantMappingsJson JSON string mapping variant names to file indices
     * @param authentication Spring Security authentication
     * @return Map from variant value name to list of ImageUploadResponse
     */
    @PostMapping(value = "/gallery/batch-variants", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<java.util.Map<String, List<ImageUploadResponse>>>> uploadBatchVariantImages(
            @PathVariable Long productId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("variantMappings") String variantMappingsJson,
            Authentication authentication) {
        
        log.info("POST /api/products/{}/images/gallery/batch-variants - Batch upload variant images", productId);
        
        Long userId = extractUserId(authentication);
        
        // Parse JSON variantMappings
        java.util.Map<String, List<Integer>> variantMappings;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            variantMappings = mapper.readValue(variantMappingsJson, 
                new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, List<Integer>>>() {});
        } catch (Exception e) {
            log.error("Failed to parse variantMappings JSON: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid variantMappings JSON format: " + e.getMessage());
        }
        
        java.util.Map<String, List<ImageUploadResponse>> response = 
            imageService.uploadBatchVariantImages(productId, files, variantMappings, userId);
        
        String message = String.format("Successfully uploaded images for %d variants (%d total images)",
                                     response.size(), 
                                     response.values().stream().mapToInt(List::size).sum());
        
        return ResponseDTO.success(response, message);
    }

    // ========== HELPER METHODS ==========

    /**
     * Extract user ID from Spring Security authentication.
     * This assumes the authentication principal contains the user ID.
     * 
     * @param authentication Spring Security Authentication
     * @return User ID (Long)
     * @throws IllegalStateException if userId cannot be extracted
     */
    private Long extractUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            throw new IllegalStateException("User not authenticated or not a JWT principal");
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return userService.extractUserIdFromJwt(jwt);
    }
}
