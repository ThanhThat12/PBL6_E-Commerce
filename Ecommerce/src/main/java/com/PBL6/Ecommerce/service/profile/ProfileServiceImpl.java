package com.PBL6.Ecommerce.service.profile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.PBL6.Ecommerce.domain.dto.AddressResponseDTO;
import com.PBL6.Ecommerce.domain.dto.profile.CompleteProfileDTO;
import com.PBL6.Ecommerce.domain.dto.profile.ProfileDTO;
import com.PBL6.Ecommerce.domain.dto.request.ChangePasswordRequest;
import com.PBL6.Ecommerce.domain.dto.request.UpdateProfileRequest;
import com.PBL6.Ecommerce.domain.entity.user.Address;
import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.exception.BadRequestException;
import com.PBL6.Ecommerce.exception.NotFoundException;
import com.PBL6.Ecommerce.repository.AddressRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.service.ImageService;
import com.PBL6.Ecommerce.domain.dto.response.ImageUploadResponse;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of ProfileService
 * Handles user profile management (basic info, avatar, password, addresses)
 * Shop profile management is handled by ShopService
 */
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;

    // ============ USER PROFILE ============

    @Override
    @Transactional(readOnly = true)
    public ProfileDTO getMyProfile(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User không tồn tại"));
        return mapToProfileDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public CompleteProfileDTO getCompleteProfile(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User không tồn tại"));
        
        CompleteProfileDTO dto = CompleteProfileDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .phoneNumber(user.getPhoneNumber())
            .avatarUrl(user.getAvatarUrl())
            .role(user.getRole().name())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
        
        // Get addresses
        List<Address> addresses = addressRepository.findByUserId(user.getId());
        dto.setAddresses(addresses.stream()
            .map(this::mapToAddressDTO)
            .collect(Collectors.toList()));
        
        return dto;
    }

    @Override
    @Transactional
    public ProfileDTO updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        // Update fields
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        user.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        return mapToProfileDTO(saved);
    }

    @Override
    @Transactional
    public ProfileDTO uploadAvatar(String username, MultipartFile file) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        // Use ImageService to upload avatar (handles deletion of old image internally)
        ImageUploadResponse uploadResponse = imageService.uploadUserAvatar(user.getId(), file);
        
        // Update user with new avatar info
        user.setAvatarUrl(uploadResponse.getUrl());
        user.setAvatarPublicId(uploadResponse.getPublicId());
        user.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        return mapToProfileDTO(saved);
    }

    @Override
    @Transactional
    public ProfileDTO deleteAvatar(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        // Use ImageService to delete avatar (handles Cloudinary deletion internally)
        if (user.getAvatarPublicId() != null) {
            try {
                imageService.deleteUserAvatar(user.getId());
            } catch (Exception e) {
                // Log but continue to set null values
                System.err.println("Failed to delete avatar from Cloudinary: " + e.getMessage());
            }
        }

        user.setAvatarUrl(null);
        user.setAvatarPublicId(null);
        user.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        return mapToProfileDTO(saved);
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu hiện tại không đúng");
        }

        // Validate new password matches confirm password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp");
        }

        // Validate new password length
        if (request.getNewPassword().length() < 6) {
            throw new BadRequestException("Mật khẩu mới phải có ít nhất 6 ký tự");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    // ============ HELPER METHODS ============

    private ProfileDTO mapToProfileDTO(User user) {
        return ProfileDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .phoneNumber(user.getPhoneNumber())
            .avatarUrl(user.getAvatarUrl())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }

    private AddressResponseDTO mapToAddressDTO(Address address) {
        AddressResponseDTO dto = new AddressResponseDTO();
        dto.setId(address.getId());
        dto.setContactName(address.getContactName());
        dto.setContactPhone(address.getContactPhone());
        dto.setProvinceId(address.getProvinceId());
        dto.setProvinceName(address.getProvinceName());
        dto.setDistrictId(address.getDistrictId());
        dto.setDistrictName(address.getDistrictName());
        dto.setWardCode(address.getWardCode());
        dto.setWardName(address.getWardName());
        dto.setFullAddress(address.getFullAddress());
        dto.setPrimaryAddress(address.isPrimaryAddress());
        dto.setTypeAddress(address.getTypeAddress());
        return dto;
    }
}
