package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.constant.TransformationType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.dto.cloudinary.CloudinaryUploadResult;
import com.PBL6.Ecommerce.dto.response.ImageUploadResponse;
import com.PBL6.Ecommerce.service.CloudinaryClient;
import com.PBL6.Ecommerce.service.UserService;
import com.PBL6.Ecommerce.util.ImageValidationUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * REST controller for generic image upload.
 * 
 * Endpoints:
 * - POST /api/images/upload - Upload image to Cloudinary with custom folder
 * 
 * Security: All endpoints require authentication.
 * This is used for KYC document uploads, temporary uploads, and other generic image needs.
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
public class CommonImageController {

    private final CloudinaryClient cloudinaryClient;
    private final ImageValidationUtil imageValidationUtil;
    private final UserService userService;

    /**
     * Upload a single image to Cloudinary.
     * Supports custom folder specification for organizing uploads.
     * 
     * Use cases:
     * - KYC document uploads (folder="kyc")
     * - Temporary uploads (folder="temp")
     * - General purpose uploads
     * 
     * @param file Image file to upload (MultipartFile)
     * @param folder Optional folder name in Cloudinary (default: "general")
     * @param authentication Spring Security Authentication object (injected)
     * @return ImageUploadResponse with URL, publicId, and metadata
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<ImageUploadResponse>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "general") String folder,
            Authentication authentication) {
        
        log.info("Generic image upload request from user: {}, folder: {}", 
                 authentication.getName(), folder);
        
        // Validate file
        imageValidationUtil.validateImage(file);
        
        // Extract userId from authenticated user
        Long userId = extractUserId(authentication);
        
        try {
            // Generate unique public ID using timestamp and user ID
            String timestamp = String.valueOf(System.currentTimeMillis());
            String publicId = String.format("%s_%d_%s", folder, userId, timestamp);
            
            // Upload to Cloudinary
            CloudinaryUploadResult result = cloudinaryClient.uploadImage(file, folder, publicId);
            
            log.info("Image uploaded successfully to folder '{}': {}", folder, result.getUrl());

            // Generate transformations
            Map<TransformationType, String> transformations =
                cloudinaryClient.generateTransformedUrls(result.getPublicId(), result.getFormat());
            
            // Ensure the original URL from the upload result is in the map.
            transformations.put(TransformationType.ORIGINAL, result.getUrl());

            // Build response
            ImageUploadResponse response = ImageUploadResponse.builder()
                    .url(result.getUrl())
                    .publicId(result.getPublicId())
                    .transformations(transformations)
                    .width(result.getWidth())
                    .height(result.getHeight())
                    .build();
            
            return ResponseDTO.success(response, "Image uploaded successfully");
            
        } catch (Exception e) {
            log.error("Failed to upload image to folder '{}': {}", folder, e.getMessage(), e);
            throw new com.PBL6.Ecommerce.exception.ImageUploadException(
                "Failed to upload image: " + e.getMessage()
            );
        }
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
