package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.dto.cloudinary.CloudinaryUploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Cloudinary Client Interface
 * Abstracts Cloudinary SDK operations for easier testing and maintenance
 */
public interface CloudinaryClient {

    /**
     * Uploads image to Cloudinary
     * 
     * @param file Image file to upload
     * @param folder Cloudinary folder path
     * @param publicId Optional custom public ID
     * @return Upload result with URL and metadata
     */
    CloudinaryUploadResult uploadImage(MultipartFile file, String folder, String publicId);

    /**
     * Deletes image from Cloudinary by public ID
     * 
     * @param publicId Cloudinary public ID
     * @return true if deleted successfully
     */
    boolean deleteImage(String publicId);

    /**
     * Destroys image from Cloudinary (hard delete)
     * 
     * @param publicId Cloudinary public ID
     * @return Deletion result map
     */
    Map<String, Object> destroyImage(String publicId);

    /**
     * Generates transformation URL for existing image
     * 
     * @param publicId Cloudinary public ID
     * @param transformation Transformation parameters (width, height, crop, etc.)
     * @return Transformed image URL
     */
    String generateTransformationUrl(String publicId, String transformation);

    /**
     * Generates all transformation URLs for an existing image.
     * 
     * @param publicId Cloudinary public ID
     * @param format   Image format (e.g., "jpg", "png")
     * @return A map of transformation types to their corresponding URLs
     */
    Map<com.PBL6.Ecommerce.constant.TransformationType, String> generateTransformedUrls(String publicId, String format);
}
