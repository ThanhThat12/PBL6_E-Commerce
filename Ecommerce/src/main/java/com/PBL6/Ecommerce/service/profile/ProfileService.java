package com.PBL6.Ecommerce.service.profile;

import org.springframework.web.multipart.MultipartFile;

import com.PBL6.Ecommerce.domain.dto.profile.CompleteProfileDTO;
import com.PBL6.Ecommerce.domain.dto.profile.ProfileDTO;
import com.PBL6.Ecommerce.domain.dto.request.ChangePasswordRequest;
import com.PBL6.Ecommerce.domain.dto.request.UpdateProfileRequest;

/**
 * Service interface for User Profile Management
 * 
 * Handles:
 * - User profile (get, update, avatar upload, password change)
 * - Complete profile (user + addresses)
 * 
 * Note: Shop profile management is handled by ShopService
 */
public interface ProfileService {
    
    // ============ USER PROFILE ============
    
    /**
     * Get current user's profile
     * 
     * @param username Current user username
     * @return ProfileDTO
     */
    ProfileDTO getMyProfile(String username);
    
    /**
     * Get complete profile including shop and addresses
     * 
     * @param username Current user username
     * @return CompleteProfileDTO with all info
     */
    CompleteProfileDTO getCompleteProfile(String username);
    
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
     * Delete user avatar
     * 
     * @param username Current user username
     * @return Updated ProfileDTO
     */
    ProfileDTO deleteAvatar(String username);
    
    /**
     * Change user password
     * 
     * @param username Current user username
     * @param request Change password request with current and new password
     */
    void changePassword(String username, ChangePasswordRequest request);
}

