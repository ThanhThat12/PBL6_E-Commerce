package com.PBL6.Ecommerce.service;

import org.springframework.web.multipart.MultipartFile;

import com.PBL6.Ecommerce.domain.dto.ChangePasswordDTO;
import com.PBL6.Ecommerce.dto.profile.PublicProfileResponse;
import com.PBL6.Ecommerce.dto.profile.UpdateUserProfileRequest;
import com.PBL6.Ecommerce.dto.profile.UserProfileResponse;

/**
 * Service interface for User Profile Management per spec 006-profile
 * 
 * Handles:
 * - Get current user profile with addresses
 * - Update profile information (fullName, email, phone) with unique checks
 * - Upload/delete avatar with Cloudinary integration
 * - Change password with validation
 * - Get public profile by username
 */
public interface UserProfileService {
    
    /**
     * Get current authenticated user's full profile
     * 
     * @return UserProfileResponse with profile + addresses
     */
    UserProfileResponse getCurrentUserProfile();
    
    /**
     * Update current user's profile information
     * Validates uniqueness of email/phone across system
     * 
     * @param request Update request with optional fullName/email/phoneNumber
     * @return Updated UserProfileResponse
     */
    UserProfileResponse updateCurrentUserProfile(UpdateUserProfileRequest request);
    
    /**
     * Upload or replace user avatar
     * - Validates file type (jpg/png/webp) and size (max 5MB)
     * - Deletes old avatar by publicId before upload
     * - Uploads to Cloudinary folder users/{userId}/avatar
     * 
     * @param file Avatar image file
     * @return Updated UserProfileResponse with new avatar
     */
    UserProfileResponse uploadAvatar(MultipartFile file);
    
    /**
     * Delete user's avatar
     * - Removes from Cloudinary by publicId
     * - Sets avatarUrl and avatarPublicId to null
     */
    void deleteAvatar();
    
    /**
     * Change user password
     * - Validates current password matches
     * - Ensures new password differs from old
     * - Encrypts and saves new password
     * 
     * @param request Change password request with old/new/confirm passwords
     */
    void changePassword(ChangePasswordDTO request);
    
    /**
     * Get public profile by username
     * Returns safe subset: username, fullName, avatarUrl, role, shopName (if SELLER)
     * 
     * @param username Target username
     * @return PublicProfileResponse with public data only
     */
    PublicProfileResponse getPublicProfile(String username);
}