package com.PBL6.Ecommerce.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.PBL6.Ecommerce.dto.response.ImageDeleteResponse;
import com.PBL6.Ecommerce.dto.response.ImageUploadResponse;
import com.PBL6.Ecommerce.service.ImageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for shop branding image management (logo and banner).
 * Handles upload, replacement, and deletion of shop logo and banner images.
 * 
 * All endpoints require SELLER or ADMIN role and validate shop ownership.
 */
@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
@Slf4j
public class ShopImageController {

    private final ImageService imageService;

    // ========== SHOP LOGO ENDPOINTS ==========

    /**
     * Upload or replace shop logo (400x400 with transparency support).
     * If a logo already exists, it will be replaced.
     * 
     * POST /api/shops/{shopId}/logo
     * 
     * @param shopId The ID of the shop
     * @param file The logo image file (MultipartFile)
     * @param authentication Current authenticated user
     * @return ImageUploadResponse with URL, public_id, and transformations
     */
    @PostMapping(value = "/{shopId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ImageUploadResponse> uploadShopLogo(
            @PathVariable Long shopId,
            @RequestPart("file") MultipartFile file,
            Authentication authentication) {
        
        log.info("Upload shop logo request for shop {} by user {}", shopId, authentication.getName());

        Long userId = extractUserId(authentication);
        ImageUploadResponse response = imageService.uploadShopLogo(shopId, file, userId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Delete shop logo.
     * Removes the logo from Cloudinary and sets shop logo fields to NULL.
     * 
     * DELETE /api/shops/{shopId}/logo
     * 
     * @param shopId The ID of the shop
     * @param authentication Current authenticated user
     * @return ImageDeleteResponse with success status
     */
    @DeleteMapping("/{shopId}/logo")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ImageDeleteResponse> deleteShopLogo(
            @PathVariable Long shopId,
            Authentication authentication) {
        
        log.info("Delete shop logo request for shop {} by user {}", shopId, authentication.getName());

        Long userId = extractUserId(authentication);
        ImageDeleteResponse response = imageService.deleteShopLogo(shopId, userId);

        return ResponseEntity.ok(response);
    }

    // ========== SHOP BANNER ENDPOINTS ==========

    /**
     * Upload or replace shop banner (1200x400 crop fill).
     * If a banner already exists, it will be replaced.
     * 
     * POST /api/shops/{shopId}/banner
     * 
     * @param shopId The ID of the shop
     * @param file The banner image file (MultipartFile)
     * @param authentication Current authenticated user
     * @return ImageUploadResponse with URL, public_id, and transformations
     */
    @PostMapping(value = "/{shopId}/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ImageUploadResponse> uploadShopBanner(
            @PathVariable Long shopId,
            @RequestPart("file") MultipartFile file,
            Authentication authentication) {
        
        log.info("Upload shop banner request for shop {} by user {}", shopId, authentication.getName());

        Long userId = extractUserId(authentication);
        ImageUploadResponse response = imageService.uploadShopBanner(shopId, file, userId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Delete shop banner.
     * Removes the banner from Cloudinary and sets shop banner fields to NULL.
     * 
     * DELETE /api/shops/{shopId}/banner
     * 
     * @param shopId The ID of the shop
     * @param authentication Current authenticated user
     * @return ImageDeleteResponse with success status
     */
    @DeleteMapping("/{shopId}/banner")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ImageDeleteResponse> deleteShopBanner(
            @PathVariable Long shopId,
            Authentication authentication) {
        
        log.info("Delete shop banner request for shop {} by user {}", shopId, authentication.getName());

        Long userId = extractUserId(authentication);
        ImageDeleteResponse response = imageService.deleteShopBanner(shopId, userId);

        return ResponseEntity.ok(response);
    }

    // ========== HELPER METHODS ==========

    /**
     * Extract user ID from authentication principal.
     * Assumes JWT authentication with user ID in the subject claim.
     * 
     * @param authentication The authentication object
     * @return The user ID
     */
    private Long extractUserId(Authentication authentication) {
        // Extract user ID from authentication principal
        // This depends on your authentication setup (JWT, OAuth2, etc.)
        // Adjust as needed based on your security configuration
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            // JWT authentication
            String userIdStr = jwt.getSubject();
            return Long.parseLong(userIdStr);
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            // UserDetails authentication (extract ID from username or custom field)
            return Long.parseLong(userDetails.getUsername());
        } else {
            // Fallback: try to parse as string
            return Long.parseLong(authentication.getName());
        }
    }
}
