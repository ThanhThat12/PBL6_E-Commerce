package com.PBL6.Ecommerce.controller.profile;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.PBL6.Ecommerce.domain.dto.ChangePasswordDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateProfileDTO;
import com.PBL6.Ecommerce.domain.dto.UserProfileDTO;
import com.PBL6.Ecommerce.domain.dto.profile.ProfileDTO;
import com.PBL6.Ecommerce.service.UserService;
import com.PBL6.Ecommerce.service.profile.ProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for user profile management operations
 * All endpoints require authentication
 */
@Tag(name = "User Profile", description = "User profile view and update (includes avatar upload)")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class ProfileController {
    
    private final UserService userService;
    private final ProfileService profileService;

    /**
     * Get current user's full profile
     * GET /api/user/profile
     * 
     * @return UserProfileDTO with all profile fields including timestamps
     */
    @GetMapping("/profile")
    public ResponseEntity<ResponseDTO<UserProfileDTO>> getProfile() {
        UserProfileDTO profile = userService.getUserProfile();
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Profile retrieved successfully", profile));
    }

    /**
     * Update current user's profile information
     * PUT /api/user/profile
     * 
     * @param dto UpdateProfileDTO containing fields to update
     * @return Updated UserProfileDTO
     */
    @PutMapping("/profile")
    public ResponseEntity<ResponseDTO<UserProfileDTO>> updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        UserProfileDTO updatedProfile = userService.updateProfile(dto);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Profile updated successfully", updatedProfile));
    }

    /**
     * Change current user's password
     * PUT /api/user/change-password
     * 
     * @param dto ChangePasswordDTO containing old and new passwords
     * @return Success message
     */
    @PutMapping("/change-password")
    public ResponseEntity<ResponseDTO<String>> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        userService.changePassword(dto);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Password changed successfully", "Password has been updated"));
    }

    /**
     * Update user's avatar URL
     * PUT /api/user/avatar
     * 
     * @param avatarUrl New avatar URL
     * @return Updated UserProfileDTO
     */
    @PutMapping("/avatar")
    public ResponseEntity<ResponseDTO<UserProfileDTO>> updateAvatar(@RequestParam String avatarUrl) {
        UserProfileDTO updatedProfile = userService.updateAvatar(avatarUrl);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Avatar updated successfully", updatedProfile));
    }

    /**
     * Upload user's avatar image file
     * POST /api/user/avatar/upload
     * 
     * Accepts multipart/form-data with "file" field
     * 
     * @param file Avatar image file (JPEG, PNG, GIF)
     * @return ProfileDTO with updated avatar URL
     */
    @Operation(summary = "Upload avatar image", description = "Upload avatar as multipart file. Returns profile with new avatar URL.")
    @PostMapping(value = "/avatar/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO<ProfileDTO>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        ProfileDTO updatedProfile = profileService.uploadAvatar(username, file);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Avatar uploaded successfully", updatedProfile));
    }
}
