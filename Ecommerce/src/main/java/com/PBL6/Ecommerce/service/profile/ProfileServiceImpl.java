package com.PBL6.Ecommerce.service.profile;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.domain.dto.request.ChangePasswordRequest;
import com.PBL6.Ecommerce.domain.dto.profile.ProfileDTO;
import com.PBL6.Ecommerce.domain.dto.request.UpdateProfileRequest;
import com.PBL6.Ecommerce.domain.dto.image.cloudinary.CloudinaryUploadResult;
import com.PBL6.Ecommerce.exception.BadRequestException;
import com.PBL6.Ecommerce.exception.NotFoundException;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.service.CloudinaryClient;
import com.PBL6.Ecommerce.util.CloudinaryUtil;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of ProfileService
 * Handles user profile management operations
 */
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryClient cloudinaryClient;
    private final CloudinaryUtil cloudinaryUtil;

    @Override
    @Transactional(readOnly = true)
    public ProfileDTO getMyProfile(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User không tồn tại"));
        return mapToDTO(user);
    }

    @Override
    @Transactional
    public ProfileDTO updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        // Update fields
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public ProfileDTO uploadAvatar(String username, MultipartFile file) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        // Delete old avatar if exists
        if (user.getAvatarPublicId() != null && !user.getAvatarPublicId().isEmpty()) {
            try {
                cloudinaryClient.deleteImage(user.getAvatarPublicId());
            } catch (Exception e) {
                // Log but continue if delete fails
                throw new BadRequestException("Không thể xóa ảnh cũ: " + e.getMessage());
            }
        }

        // Upload to Cloudinary
        String folder = cloudinaryUtil.getFolderPath("avatars");
        String publicId = String.format("user_%d_avatar_%d", user.getId(), System.currentTimeMillis());
        
        CloudinaryUploadResult result;
        try {
            result = cloudinaryClient.uploadImage(file, folder, publicId);
        } catch (Exception e) {
            throw new BadRequestException("Không thể tải ảnh lên: " + e.getMessage());
        }

        user.setAvatarUrl(result.getSecureUrl());
        user.setAvatarPublicId(result.getPublicId());
        user.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        return mapToDTO(saved);
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

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    /**
     * Map User entity to ProfileDTO
     * 
     * @param user User entity
     * @return ProfileDTO
     */
    private ProfileDTO mapToDTO(User user) {
        return ProfileDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .phoneNumber(user.getPhoneNumber())
            .avatarUrl(user.getAvatarUrl())
            .activated(user.isActivated())
            .role(user.getRole() != null ? user.getRole().name() : null)
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
