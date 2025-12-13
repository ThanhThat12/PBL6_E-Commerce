package com.PBL6.Ecommerce.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.PBL6.Ecommerce.domain.Address;
import com.PBL6.Ecommerce.domain.Role;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.AddressResponseDTO;
import com.PBL6.Ecommerce.domain.dto.ChangePasswordDTO;
import com.PBL6.Ecommerce.dto.cloudinary.CloudinaryUploadResult;
import com.PBL6.Ecommerce.dto.profile.PublicProfileResponse;
import com.PBL6.Ecommerce.dto.profile.UpdateUserProfileRequest;
import com.PBL6.Ecommerce.dto.profile.UserProfileResponse;
import com.PBL6.Ecommerce.exception.BadRequestException;
import com.PBL6.Ecommerce.exception.NotFoundException;
import com.PBL6.Ecommerce.exception.UnauthenticatedException;
import com.PBL6.Ecommerce.repository.AddressRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.service.CloudinaryClient;
import com.PBL6.Ecommerce.service.UserProfileService;
import com.PBL6.Ecommerce.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of UserProfileService per spec 006-profile
 * Integrates with existing UserService for Authentication handling
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CloudinaryClient cloudinaryClient;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        User user = getCurrentAuthenticatedUser();
        return toUserProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateCurrentUserProfile(UpdateUserProfileRequest request) {
        User user = getCurrentAuthenticatedUser();

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new BadRequestException("Phone number already exists");
            }
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        userRepository.save(user);
        log.info("Profile updated for user: {}", user.getUsername());
        return toUserProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse uploadAvatar(MultipartFile file) {
        validateAvatar(file);
        User user = getCurrentAuthenticatedUser();

        String folder = String.format("users/%d/avatar", user.getId());
        
        // Delete old avatar if exists
        if (user.getAvatarPublicId() != null) {
            try {
                cloudinaryClient.deleteImage(user.getAvatarPublicId());
                log.info("Deleted old avatar: {}", user.getAvatarPublicId());
            } catch (Exception e) {
                log.warn("Failed to delete old avatar: {}", user.getAvatarPublicId(), e);
            }
        }

        // Upload new avatar
        CloudinaryUploadResult uploadResult = cloudinaryClient.uploadImage(file, folder, null);
        user.setAvatarUrl(uploadResult.getSecureUrl());
        user.setAvatarPublicId(uploadResult.getPublicId());
        userRepository.save(user);

        log.info("Avatar uploaded for user: {}, publicId: {}", user.getUsername(), uploadResult.getPublicId());
        return toUserProfileResponse(user);
    }

    @Override
    @Transactional
    public void deleteAvatar() {
        User user = getCurrentAuthenticatedUser();
        
        if (user.getAvatarPublicId() != null) {
            try {
                cloudinaryClient.deleteImage(user.getAvatarPublicId());
                log.info("Deleted avatar: {}", user.getAvatarPublicId());
            } catch (Exception e) {
                log.warn("Failed to delete avatar: {}", user.getAvatarPublicId(), e);
            }
            
            user.setAvatarUrl(null);
            user.setAvatarPublicId(null);
            userRepository.save(user);
            log.info("Avatar removed for user: {}", user.getUsername());
        }
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordDTO request) {
        User user = getCurrentAuthenticatedUser();
        
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirmation do not match");
        }
        
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from old password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        PublicProfileResponse response = new PublicProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setRole(user.getRole());
        response.setShopName(user.getShop() != null ? user.getShop().getName() : null);
        
        log.debug("Public profile retrieved for user: {}", username);
        return response;
    }

    /**
     * Get current authenticated user using existing UserService
     */
    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthenticated access attempt");
            throw new UnauthenticatedException("Authentication required");
        }
        
        return userService.resolveCurrentUser(authentication);
    }

    /**
     * Validate avatar file type and size
     */
    private void validateAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") || 
            contentType.equals("image/png") || contentType.equals("image/webp"))) {
            throw new BadRequestException("Invalid file type. Only JPEG, PNG, WEBP are allowed");
        }

        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new BadRequestException("File size exceeds 5MB limit");
        }
    }

    /**
     * Map User entity to UserProfileResponse with addresses
     */
    private UserProfileResponse toUserProfileResponse(User user) {
        List<Address> addresses = addressRepository.findByUserId(user.getId());
        
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        response.setAddresses(addresses.stream().map(this::toAddressDTO).collect(Collectors.toList()));
        
        return response;
    }

    /**
     * Map Address entity to AddressResponseDTO
     */
    private AddressResponseDTO toAddressDTO(Address address) {
        AddressResponseDTO dto = new AddressResponseDTO();
        dto.setId(address.getId());
        dto.setFullAddress(address.getFullAddress());
        dto.setProvinceId(address.getProvinceId());
        dto.setDistrictId(address.getDistrictId());
        dto.setWardCode(address.getWardCode());
        dto.setProvinceName(address.getProvinceName());
        dto.setDistrictName(address.getDistrictName());
        dto.setWardName(address.getWardName());
        dto.setContactName(address.getContactName());
        dto.setContactPhone(address.getContactPhone());
        dto.setPrimaryAddress(address.isPrimaryAddress());
        dto.setCreatedAt(address.getCreatedAt());
        return dto;
    }
}
