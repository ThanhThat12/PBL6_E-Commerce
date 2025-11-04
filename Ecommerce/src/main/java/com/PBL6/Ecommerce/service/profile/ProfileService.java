package com.PBL6.Ecommerce.service.profile;

import org.springframework.web.multipart.MultipartFile;

import com.PBL6.Ecommerce.dto.profile.request.ChangePasswordRequest;
import com.PBL6.Ecommerce.dto.profile.ProfileDTO;
import com.PBL6.Ecommerce.dto.profile.request.UpdateProfileRequest;

/**
 * Service interface for Profile Management
 * 
 * Handles:
 * - Get user profile
 * - Update profile information
 * - Upload avatar
 * - Change password
 */
public interface ProfileService {
    
    /**
     * Get current user's profile
     * 
     * @param username Current user username
     * @return ProfileDTO
     */
    ProfileDTO getMyProfile(String username);
    
    /**
     * Update user profile information
     * 
     * @param username Current user username
     * @param request Update request with fullName and phoneNumber
     * @return Updated ProfileDTO
     */
    ProfileDTO updateProfile(String username, UpdateProfileRequest request);
    
    /**
     * Upload user avatar image
     * 
     * @param username Current user username
     * @param file Avatar image file
     * @return Updated ProfileDTO with new avatar URL
     */
    ProfileDTO uploadAvatar(String username, MultipartFile file);
    
    /**
     * Change user password
     * 
     * @param username Current user username
     * @param request Change password request with current and new password
     */
    void changePassword(String username, ChangePasswordRequest request);
}
