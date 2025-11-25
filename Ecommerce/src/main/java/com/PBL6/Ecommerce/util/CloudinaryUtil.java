package com.PBL6.Ecommerce.util;

import com.PBL6.Ecommerce.config.ImageUploadConfig;
import com.PBL6.Ecommerce.constant.TransformationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Cloudinary Utility
 * Builds transformation URLs and manages Cloudinary operations
 */
@Component
@RequiredArgsConstructor
public class CloudinaryUtil {

    private final ImageUploadConfig config;

    /**
     * Builds transformation URL for a given public ID and transformation type
     * 
     * @param publicId Cloudinary public ID
     * @param transformationType Type of transformation
     * @return Transformed image URL
     */
    public String buildTransformationUrl(String publicId, TransformationType transformationType) {
        if (publicId == null || publicId.isEmpty()) {
            return null;
        }

        ImageUploadConfig.Transformation transformation = getTransformation(transformationType);
        if (transformation == null) {
            return publicId; // Return original if no transformation found
        }

        // Build transformation URL pattern: 
        // https://res.cloudinary.com/{cloud-name}/image/upload/w_{width},h_{height},c_{crop}/{publicId}
        StringBuilder url = new StringBuilder();
        url.append("w_").append(transformation.getWidth())
           .append(",h_").append(transformation.getHeight())
           .append(",c_").append(transformation.getCrop());
        
        // Add gravity for face detection (avatar)
        if (transformation.getGravity() != null && !transformation.getGravity().isEmpty()) {
            url.append(",g_").append(transformation.getGravity());
        }

        return url.toString();
    }

    /**
     * Gets all transformations for an image
     * Returns map of transformation type to URL parameters
     */
    public Map<TransformationType, String> buildAllTransformations(String publicId) {
        Map<TransformationType, String> transformations = new HashMap<>();
        
        transformations.put(TransformationType.THUMBNAIL, 
            buildTransformationUrl(publicId, TransformationType.THUMBNAIL));
        transformations.put(TransformationType.MEDIUM, 
            buildTransformationUrl(publicId, TransformationType.MEDIUM));
        transformations.put(TransformationType.LARGE, 
            buildTransformationUrl(publicId, TransformationType.LARGE));
        transformations.put(TransformationType.ORIGINAL, publicId);
        
        return transformations;
    }

    /**
     * Gets transformation configuration by type
     */
    private ImageUploadConfig.Transformation getTransformation(TransformationType type) {
        Map<String, ImageUploadConfig.Transformation> transformations = config.getTransformations();
        
        return switch (type) {
            case THUMBNAIL -> transformations.get("thumbnail");
            case MEDIUM -> transformations.get("medium");
            case LARGE -> transformations.get("large");
            case ORIGINAL -> null; // No transformation for original
        };
    }

    /**
     * Builds folder path for image upload based on entity type
     */
    public String getFolderPath(String entityType) {
        Map<String, String> folderPaths = config.getFolderPaths();
        return folderPaths.getOrDefault(entityType, "misc");
    }

    /**
     * Extracts public ID from Cloudinary URL
     */
    public String extractPublicId(String cloudinaryUrl) {
        if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
            return null;
        }

        // Cloudinary URL format: https://res.cloudinary.com/{cloud-name}/image/upload/{transformations}/{publicId}.{format}
        // Extract publicId from URL
        try {
            String[] parts = cloudinaryUrl.split("/");
            String lastPart = parts[parts.length - 1]; // Get filename with extension
            return lastPart.substring(0, lastPart.lastIndexOf(".")); // Remove extension
        } catch (Exception e) {
            return null;
        }
    }
}
