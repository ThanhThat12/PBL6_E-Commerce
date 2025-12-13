package com.PBL6.Ecommerce.controller.profile;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.PBL6.Ecommerce.domain.dto.profile.request.ChangePasswordRequest;
import com.PBL6.Ecommerce.domain.dto.profile.ProfileDTO;
import com.PBL6.Ecommerce.domain.dto.profile.request.UpdateProfileRequest;
import com.PBL6.Ecommerce.service.profile.ProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for Profile Management (Buyer/Seller)
 * 
 * Endpoints:
 * - GET /api/profile - Get current user's profile
 * - PUT /api/profile - Update profile information
 * - POST /api/profile/avatar - Upload avatar image
 * - PUT /api/profile/password - Change password
 * 
 * All endpoints require BUYER or SELLER role authentication
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileManagementController {

    private final ProfileService profileService;

    /**
     * GET /api/profile
     * 
     * Get current user's profile information
     * 
     * @param jwt Current user JWT token
     * @return ProfileDTO with user information
     */
    @GetMapping
    @PreAuthorize("hasRole('BUYER') or hasRole('SELLER')")
    public ResponseEntity<ProfileDTO> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaim("username");
        ProfileDTO profile = profileService.getMyProfile(username);
        return ResponseEntity.ok(profile);
    }

    /**
     * PUT /api/profile
     * 
     * Update profile information (fullName, phoneNumber)
     * 
     * Request body:
     * {
     *   "fullName": "Nguyễn Văn A",
     *   "phoneNumber": "0123456789"
     * }
     * 
     * @param request Update profile request
     * @param jwt Current user JWT token
     * @return Updated ProfileDTO
     */
    @PutMapping
    @PreAuthorize("hasRole('BUYER') or hasRole('SELLER')")
    public ResponseEntity<ProfileDTO> updateProfile(
            @RequestBody @Valid UpdateProfileRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaim("username");
        ProfileDTO updated = profileService.updateProfile(username, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * POST /api/profile/avatar
     * 
     * Upload user avatar image
     * 
     * Validation:
     * - Max size: 5MB
     * - Only image files (JPEG, PNG, WebP, GIF)
     * 
     * @param file Avatar image file
     * @param jwt Current user JWT token
     * @return Updated ProfileDTO with new avatar URL
     */
    @PostMapping("/avatar")
    @PreAuthorize("hasRole('BUYER') or hasRole('SELLER')")
    public ResponseEntity<ProfileDTO> uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaim("username");
        ProfileDTO updated = profileService.uploadAvatar(username, file);
        return ResponseEntity.ok(updated);
    }

    /**
     * PUT /api/profile/password
     * 
     * Change user password
     * 
     * Request body:
     * {
     *   "currentPassword": "oldpass123",
     *   "newPassword": "newpass123",
     *   "confirmPassword": "newpass123"
     * }
     * 
     * Validation:
     * - Current password must be correct
     * - New password must be at least 6 characters
     * - New password must match confirm password
     * 
     * @param request Change password request
     * @param jwt Current user JWT token
     * @return Success message
     */
    @PutMapping("/password")
    @PreAuthorize("hasRole('BUYER') or hasRole('SELLER')")
    public ResponseEntity<ApiResponse> changePassword(
            @RequestBody @Valid ChangePasswordRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaim("username");
        profileService.changePassword(username, request);
        return ResponseEntity.ok(new ApiResponse("Đổi mật khẩu thành công"));
    }

    /**
     * Simple API response wrapper
     */
    public record ApiResponse(String message) {}
}
