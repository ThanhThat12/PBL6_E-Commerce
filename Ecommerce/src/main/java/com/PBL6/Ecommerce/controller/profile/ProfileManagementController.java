package com.PBL6.Ecommerce.controller.profile;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.profile.CompleteProfileDTO;
import com.PBL6.Ecommerce.domain.dto.profile.ProfileDTO;
import com.PBL6.Ecommerce.domain.dto.request.ChangePasswordRequest;
import com.PBL6.Ecommerce.domain.dto.request.UpdateProfileRequest;
import com.PBL6.Ecommerce.service.profile.ProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for User Profile Management
 * 
 * User Profile Endpoints:
 * - GET /api/profile - Get user profile
 * - GET /api/profile/complete - Get complete profile (user + addresses)
 * - PUT /api/profile - Update user profile
 * - POST /api/profile/avatar - Upload avatar
 * - DELETE /api/profile/avatar - Delete avatar
 * - PUT /api/profile/password - Change password
 * 
 * Note: Shop profile management is handled by ShopController
 */
@Tag(name = "Profile Management", description = "User profile management (basic info, avatar, password, addresses)")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileManagementController {

    private final ProfileService profileService;

    // ============ USER PROFILE ENDPOINTS ============

    @Operation(
        summary = "Get user profile",
        description = "Get current user's basic profile information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved profile"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<ResponseDTO<ProfileDTO>> getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        ProfileDTO profile = profileService.getMyProfile(username);
        return ResponseDTO.success(profile, "Lấy thông tin profile thành công");
    }

    @Operation(
        summary = "Get complete profile",
        description = "Get complete profile including user info and addresses. Shop info is available via ShopController."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved complete profile",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Complete Profile Response",
                    value = "{\"status\":200,\"error\":null,\"message\":\"Success\",\"data\":{\"id\":1,\"username\":\"seller1\",\"email\":\"seller@example.com\",\"fullName\":\"Nguyễn Văn A\",\"phoneNumber\":\"0912345678\",\"avatarUrl\":\"https://...\",\"role\":\"SELLER\",\"shop\":{\"id\":1,\"name\":\"Shop ABC\",\"description\":\"Shop description\",\"logoUrl\":\"https://...\",\"status\":\"ACTIVE\"},\"addresses\":[{\"id\":1,\"contactName\":\"Nguyễn Văn A\",\"contactPhone\":\"0912345678\",\"fullAddress\":\"123 ABC Street\",\"typeAddress\":\"HOME\",\"primaryAddress\":true}]}}"
                )
            )
        )
    })
    @GetMapping("/complete")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<ResponseDTO<CompleteProfileDTO>> getCompleteProfile(Authentication authentication) {
        String username = authentication.getName();
        CompleteProfileDTO profile = profileService.getCompleteProfile(username);
        return ResponseDTO.success(profile, "Lấy thông tin profile đầy đủ thành công");
    }

    @Operation(
        summary = "Update user profile",
        description = "Update user profile information (fullName, phoneNumber)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated profile"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PutMapping
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<ResponseDTO<ProfileDTO>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        ProfileDTO updated = profileService.updateProfile(username, request);
        return ResponseDTO.success(updated, "Cập nhật profile thành công");
    }

    @Operation(
        summary = "Upload avatar",
        description = "Upload user avatar image. Max size: 5MB. Supported formats: JPEG, PNG, WebP, GIF"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully uploaded avatar"),
        @ApiResponse(responseCode = "400", description = "Invalid file or file too large")
    })
    @PostMapping(value = "/avatar", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<ResponseDTO<ProfileDTO>> uploadAvatar(
            @Parameter(
                description = "Avatar image file", 
                required = true,
                content = @Content(mediaType = "multipart/form-data")
            ) 
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        String username = authentication.getName();
        ProfileDTO updated = profileService.uploadAvatar(username, file);
        return ResponseDTO.success(updated, "Upload avatar thành công");
    }

    @Operation(
        summary = "Delete avatar",
        description = "Delete current user's avatar image"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully deleted avatar")
    })
    @DeleteMapping("/avatar")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<ResponseDTO<ProfileDTO>> deleteAvatar(Authentication authentication) {
        String username = authentication.getName();
        ProfileDTO updated = profileService.deleteAvatar(username);
        return ResponseDTO.success(updated, "Xóa avatar thành công");
    }

    @Operation(
        summary = "Change password",
        description = "Change user password. Requires current password verification."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully changed password"),
        @ApiResponse(responseCode = "400", description = "Current password incorrect or new password invalid")
    })
    @PutMapping("/password")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<ResponseDTO<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        profileService.changePassword(username, request);
        return ResponseDTO.success("Password changed", "Đổi mật khẩu thành công");
    }
}
