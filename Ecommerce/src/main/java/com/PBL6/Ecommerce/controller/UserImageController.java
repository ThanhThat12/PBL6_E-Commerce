package com.PBL6.Ecommerce.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * REST controller for user avatar management.
 * 
 * Endpoints:
 * - POST /api/users/me/avatar - Upload or replace user avatar
 * - DELETE /api/users/me/avatar - Delete user avatar
 * 
 * Security: All endpoints require authentication.
 * Users can only manage their own avatars (ownership enforced by extracting userId from Authentication).
 */
@Tag(name = "User Images", description = "User avatar upload")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserImageController {

    private final ImageService imageService;
    private final UserService userService;

    /**
     * Upload or replace user avatar.
     * Applies face detection transformation (300x300).
     * If user already has an avatar, it will be replaced (old avatar deleted from Cloudinary).
     * 
     * @param file Avatar image file (MultipartFile)
     * @param authentication Spring Security Authentication object (injected)
     * @return ImageUploadResponse with URL, public_id, and transformation URLs
     */
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<ImageUploadResponse>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        log.info("Upload avatar request from user: {}", authentication.getName());
        
        // Extract userId from authenticated user
        Long userId = extractUserId(authentication);
        
        // Upload avatar via service
        ImageUploadResponse response = imageService.uploadUserAvatar(userId, file);
        
        return ResponseDTO.success(response, "Avatar uploaded successfully");
    }

    /**
     * Delete user avatar.
     * Removes avatar from Cloudinary and sets avatar fields to null in database.
     * Frontend should display a placeholder image when avatarUrl is null.
     * 
     * @param authentication Spring Security Authentication object (injected)
     * @return ImageDeleteResponse with success status
     */
    @DeleteMapping("/me/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<ImageDeleteResponse>> deleteAvatar(Authentication authentication) {
        
        log.info("Delete avatar request from user: {}", authentication.getName());
        
        // Extract userId from authenticated user
        Long userId = extractUserId(authentication);
        
        // Delete avatar via service
        ImageDeleteResponse response = imageService.deleteUserAvatar(userId);
        
        return ResponseDTO.success(response, "Avatar deleted successfully");
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
