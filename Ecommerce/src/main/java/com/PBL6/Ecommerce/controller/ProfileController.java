package com.PBL6.Ecommerce.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.dto.ChangePasswordDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateProfileDTO;
import com.PBL6.Ecommerce.domain.dto.UserProfileDTO;
import com.PBL6.Ecommerce.service.UserService;

import jakarta.validation.Valid;

/**
 * Controller for user profile management operations
 * All endpoints require authentication
 */
@Tag(name = "User Profile", description = "User profile view and update")
@RestController
@RequestMapping("/api/user")
public class ProfileController {
    
    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

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
}
