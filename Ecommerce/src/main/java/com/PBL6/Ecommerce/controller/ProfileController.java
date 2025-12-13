package com.PBL6.Ecommerce.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.PBL6.Ecommerce.domain.dto.ChangePasswordDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.dto.profile.PublicProfileResponse;
import com.PBL6.Ecommerce.dto.profile.UpdateUserProfileRequest;
import com.PBL6.Ecommerce.dto.profile.UserProfileResponse;
import com.PBL6.Ecommerce.service.UserProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Profile Controller per spec 006-profile
 * Handles user profile management: view, update, avatar upload/delete, change password, public profile
 * All endpoints require authentication (BUYER/SELLER roles)
 */
@Tag(name = "Profile", description = "User profile management endpoints")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    
    private final UserProfileService userProfileService;

    @Operation(summary = "Get current user profile", description = "Returns full profile including addresses")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    @GetMapping
    public ResponseEntity<ResponseDTO<UserProfileResponse>> getCurrentProfile() {
        UserProfileResponse profile = userProfileService.getCurrentUserProfile();
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Profile retrieved successfully", profile));
    }

    @Operation(summary = "Get public profile by username", description = "Returns public profile data only")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Public profile retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{username}")
    public ResponseEntity<ResponseDTO<PublicProfileResponse>> getPublicProfile(@PathVariable String username) {
        PublicProfileResponse profile = userProfileService.getPublicProfile(username);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Public profile retrieved successfully", profile));
    }

    @Operation(summary = "Update profile information", description = "Update fullName, email, or phoneNumber")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error or duplicate email/phone"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    @PutMapping
    public ResponseEntity<ResponseDTO<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateUserProfileRequest request) {
        UserProfileResponse updatedProfile = userProfileService.updateCurrentUserProfile(request);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Profile updated successfully", updatedProfile));
    }

    @Operation(summary = "Upload avatar", description = "Upload or replace user avatar (max 5MB, jpg/png/webp)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Avatar uploaded successfully", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserProfileResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid file type or size exceeds limit"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO<UserProfileResponse>> uploadAvatar(
            @RequestPart("file") MultipartFile file) {
        UserProfileResponse updatedProfile = userProfileService.uploadAvatar(file);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Avatar uploaded successfully", updatedProfile));
    }

    @Operation(summary = "Delete avatar", description = "Remove user avatar and restore to default")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Avatar deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    @DeleteMapping("/avatar")
    public ResponseEntity<Void> deleteAvatar() {
        userProfileService.deleteAvatar();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Change password", description = "Change user password with validation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Old password incorrect or validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    @PostMapping("/change-password")
    public ResponseEntity<ResponseDTO<String>> changePassword(@Valid @RequestBody ChangePasswordDTO request) {
        userProfileService.changePassword(request);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Password changed successfully", "Password has been updated"));
    }
}
