package com.PBL6.Ecommerce.service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.PBL6.Ecommerce.exception.BadRequestException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

/**
 * Service for handling file uploads to Cloudinary
 * 
 * Supports:
 * - Avatar images
 * - Product images
 * - Review images
 */
@Service
public class CloudinaryService {
    
    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);
    
    @Autowired
    private Cloudinary cloudinary;
    
    // File size limit: 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    // Allowed image types
    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"};
    
    /**
     * Upload avatar image to Cloudinary
     * 
     * @param file Avatar image file
     * @param userId User ID for organizing
     * @return Secure URL of uploaded image
     */
    public String uploadAvatar(MultipartFile file, Long userId) {
        validateImageFile(file);
        
        try {
            String publicId = "ecommerce/avatars/user_" + userId + "_" + UUID.randomUUID().toString();
            
            Map<String, Object> params = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", "ecommerce/avatars",
                "resource_type", "image",
                "transformation", new com.cloudinary.Transformation()
                    .width(300).height(300)
                    .crop("fill")
                    .gravity("face")
                    .quality("auto")
                    .fetchFormat("auto")
            );
            
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            String secureUrl = (String) uploadResult.get("secure_url");
            
            logger.info("Successfully uploaded avatar for user {}: {}", userId, secureUrl);
            return secureUrl;
            
        } catch (IOException e) {
            logger.error("Failed to upload avatar for user {}: {}", userId, e.getMessage(), e);
            throw new BadRequestException("Lỗi khi upload ảnh đại diện: " + e.getMessage());
        }
    }
    
    /**
     * Upload product image to Cloudinary
     * 
     * @param file Product image file
     * @param productId Product ID for organizing
     * @return Secure URL of uploaded image
     */
    public String uploadProductImage(MultipartFile file, Long productId) {
        validateImageFile(file);
        
        try {
            String publicId = "ecommerce/products/product_" + productId + "_" + UUID.randomUUID().toString();
            
            Map<String, Object> params = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", "ecommerce/products",
                "resource_type", "image",
                "transformation", new com.cloudinary.Transformation()
                    .width(800).height(800)
                    .crop("limit")
                    .quality("auto")
                    .fetchFormat("auto")
            );
            
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            String secureUrl = (String) uploadResult.get("secure_url");
            
            logger.info("Successfully uploaded product image for product {}: {}", productId, secureUrl);
            return secureUrl;
            
        } catch (IOException e) {
            logger.error("Failed to upload product image for product {}: {}", productId, e.getMessage(), e);
            throw new BadRequestException("Lỗi khi upload ảnh sản phẩm: " + e.getMessage());
        }
    }
    
    /**
     * Upload review image to Cloudinary
     * 
     * @param file Review image file
     * @param userId User ID for organizing
     * @return Secure URL of uploaded image
     */
    public String uploadReviewImage(MultipartFile file, Long userId) {
        validateImageFile(file);
        
        try {
            String publicId = "ecommerce/reviews/user_" + userId + "_" + UUID.randomUUID().toString();
            
            Map<String, Object> params = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", "ecommerce/reviews",
                "resource_type", "image",
                "transformation", new com.cloudinary.Transformation()
                    .width(600).height(600)
                    .crop("limit")
                    .quality("auto")
                    .fetchFormat("auto")
            );
            
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            String secureUrl = (String) uploadResult.get("secure_url");
            
            logger.info("Successfully uploaded review image for user {}: {}", userId, secureUrl);
            return secureUrl;
            
        } catch (IOException e) {
            logger.error("Failed to upload review image for user {}: {}", userId, e.getMessage(), e);
            throw new BadRequestException("Lỗi khi upload ảnh đánh giá: " + e.getMessage());
        }
    }
    
    /**
     * Delete image from Cloudinary by URL
     * 
     * @param imageUrl Full URL of the image
     * @return true if deleted successfully
     */
    public boolean deleteImage(String imageUrl) {
        try {
            // Extract public_id from URL
            // URL format: https://res.cloudinary.com/{cloud_name}/image/upload/v{version}/{public_id}.{format}
            String publicId = extractPublicIdFromUrl(imageUrl);
            
            if (publicId == null) {
                logger.warn("Could not extract public_id from URL: {}", imageUrl);
                return false;
            }
            
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String resultStatus = (String) result.get("result");
            
            boolean success = "ok".equals(resultStatus);
            logger.info("Delete image {}: {}", publicId, success ? "SUCCESS" : "FAILED");
            
            return success;
            
        } catch (IOException e) {
            logger.error("Failed to delete image {}: {}", imageUrl, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Extract public_id from Cloudinary URL
     * 
     * @param imageUrl Cloudinary URL
     * @return public_id or null if not found
     */
    private String extractPublicIdFromUrl(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return null;
        }
        
        try {
            // Split by /upload/
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) {
                return null;
            }
            
            // Get part after /upload/v{version}/
            String afterUpload = parts[1];
            int versionIndex = afterUpload.indexOf('/');
            if (versionIndex == -1) {
                return null;
            }
            
            String pathAfterVersion = afterUpload.substring(versionIndex + 1);
            
            // Remove file extension
            int dotIndex = pathAfterVersion.lastIndexOf('.');
            if (dotIndex != -1) {
                pathAfterVersion = pathAfterVersion.substring(0, dotIndex);
            }
            
            return pathAfterVersion;
            
        } catch (Exception e) {
            logger.error("Error extracting public_id from URL {}: {}", imageUrl, e.getMessage());
            return null;
        }
    }
    
    /**
     * Validate image file
     * 
     * @param file File to validate
     * @throws BadRequestException if validation fails
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File không được để trống");
        }
        
        // Check file size (max 5MB)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Kích thước file không được vượt quá 5MB");
        }
        
        // Check content type
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new BadRequestException("Không xác định được loại file");
        }
        
        boolean isValidType = false;
        for (String allowedType : ALLOWED_TYPES) {
            if (contentType.equals(allowedType)) {
                isValidType = true;
                break;
            }
        }
        
        if (!isValidType) {
            throw new BadRequestException("Chỉ chấp nhận file ảnh định dạng: JPEG, PNG, GIF, WEBP");
        }
        
        // Check file name
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BadRequestException("Tên file không hợp lệ");
        }
    }
}
