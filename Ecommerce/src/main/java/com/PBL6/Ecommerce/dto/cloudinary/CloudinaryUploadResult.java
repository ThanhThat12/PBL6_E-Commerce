package com.PBL6.Ecommerce.dto.cloudinary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cloudinary Upload Result DTO
 * Represents the result of a Cloudinary upload operation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloudinaryUploadResult {
    
    /**
     * Cloudinary public ID (used for deletion and transformations)
     */
    private String publicId;
    
    /**
     * Full URL of uploaded image
     */
    private String url;
    
    /**
     * Secure HTTPS URL
     */
    private String secureUrl;
    
    /**
     * Image format (jpg, png, etc.)
     */
    private String format;
    
    /**
     * Image width in pixels
     */
    private Integer width;
    
    /**
     * Image height in pixels
     */
    private Integer height;
    
    /**
     * File size in bytes
     */
    private Long bytes;
    
    /**
     * Cloudinary resource type (image, video, etc.)
     */
    private String resourceType;
}
