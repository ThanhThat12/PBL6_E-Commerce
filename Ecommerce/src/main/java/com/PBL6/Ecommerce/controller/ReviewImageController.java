package com.PBL6.Ecommerce.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.dto.response.ImageDeleteResponse;
import com.PBL6.Ecommerce.dto.response.ImageUploadResponse;
import com.PBL6.Ecommerce.service.ImageService;
import com.PBL6.Ecommerce.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for review image management.
 * 
 * Endpoints:
 * - POST /api/reviews/{id}/images - Upload images to a review (up to 5)
 * - DELETE /api/reviews/{id}/images/{index} - Delete image from review by index
 * 
 * Security: All endpoints require authentication.
 * Users can only manage images on their own reviews (ownership enforced by service layer).
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewImageController {

    private final ImageService imageService;
    private final UserService userService;

    /**
     * Upload images to a product review (up to 5 images total).
     * Images are stored as JSON array in the review's images column.
     * Each upload can contain 1-5 images, but total cannot exceed 5.
     * 
     * @param reviewId The ID of the review
     * @param files List of image files (1-5 files)
     * @param authentication Spring Security Authentication object (injected)
     * @return List of ImageUploadResponse with URLs and metadata
     */
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<List<ImageUploadResponse>>> uploadReviewImages(
            @PathVariable("id") Long reviewId,
            @RequestParam("files") List<MultipartFile> files,
            Authentication authentication) {
        
        log.info("Upload review images request for review {} from user: {}", reviewId, authentication.getName());
        
        // Extract userId from authenticated user
        Long userId = extractUserId(authentication);
        
        // Upload images via service
        List<ImageUploadResponse> responses = imageService.uploadReviewImages(reviewId, files, userId);
        
        return ResponseDTO.success(responses, 
            String.format("Successfully uploaded %d image(s) to review", responses.size()));
    }

    /**
     * Delete a single image from a review by its index.
     * Index is 0-based (first image is index 0).
     * After deletion, remaining images keep their current indices.
     * 
     * @param reviewId The ID of the review
     * @param imageIndex The index of the image to delete (0-based)
     * @param authentication Spring Security Authentication object (injected)
     * @return ImageDeleteResponse with success status
     */
    @DeleteMapping("/{id}/images/{index}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<ImageDeleteResponse>> deleteReviewImage(
            @PathVariable("id") Long reviewId,
            @PathVariable("index") Integer imageIndex,
            Authentication authentication) {
        
        log.info("Delete review image request for review {} index {} from user: {}", 
                 reviewId, imageIndex, authentication.getName());
        
        // Extract userId from authenticated user
        Long userId = extractUserId(authentication);
        
        // Delete image via service
        ImageDeleteResponse response = imageService.deleteReviewImage(reviewId, imageIndex, userId);
        
        return ResponseDTO.success(response, "Review image deleted successfully");
    }

    /**
     * Extract userId from Spring Security Authentication object.
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
