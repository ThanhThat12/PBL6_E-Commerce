package com.PBL6.Ecommerce.controller;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
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
import com.PBL6.Ecommerce.domain.dto.TempImageUploadResponseDTO;
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
 * - POST /api/reviews/images/upload - Upload temporary images BEFORE creating review
 * - POST /api/reviews/{id}/images - Upload images to an existing review (up to 5)
 * - DELETE /api/reviews/{id}/images/{index} - Delete image from review by index
 * 
 * Security: All endpoints require authentication.
 * Users can only manage images on their own reviews (ownership enforced by service layer).
 */
@Tag(name = "Review Images", description = "Review image upload")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewImageController {

    private final ImageService imageService;
    private final UserService userService;

    /**
     * Upload images TEMPORARILY before creating a review.
     * Images are uploaded to Cloudinary with folder "review-temp".
     * Frontend stores the returned URLs and sends them when creating the review.
     * 
     * Flow:
     * 1. User selects images
     * 2. Frontend calls POST /api/reviews/images/upload
     * 3. Returns URLs array
     * 4. Frontend calls POST /api/products/{productId}/reviews with { images: [urls] }
     * 
     * @param files List of image files (1-5 files)
     * @param authentication Spring Security Authentication object (injected)
     * @return List of TempImageUploadResponseDTO with URLs
     */
    @PostMapping(value = "/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<List<TempImageUploadResponseDTO>>> uploadTempReviewImages(
            @RequestParam("files") List<MultipartFile> files,
            Authentication authentication) {
        
        log.info("Upload temp review images request from user: {}, file count: {}", 
                 authentication.getName(), files != null ? files.size() : 0);
        
        // Extract userId from authenticated user
        Long userId = extractUserId(authentication);
        
        // Upload images via service (temporary folder)
        List<TempImageUploadResponseDTO> responses = imageService.uploadTempReviewImages(files, userId);
        
        return ResponseDTO.success(responses, 
            String.format("Tải lên %d ảnh thành công. Hãy sử dụng URLs khi gửi đánh giá.", responses.size()));
    }

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
