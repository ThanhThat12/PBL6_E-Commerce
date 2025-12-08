package com.PBL6.Ecommerce.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * Image Upload Configuration Properties
 * Maps properties from application.properties with prefix "image.upload"
 */
@Configuration
@ConfigurationProperties(prefix = "image.upload")
@Getter
@Setter
public class ImageUploadConfig {

    private long maxSize; // Max file size in bytes
    private List<String> allowedFormats; // Allowed file formats
    private Dimensions minDimensions; // Minimum image dimensions
    private Dimensions maxDimensions; // Maximum image dimensions
    private Map<String, String> folderPaths; // Cloudinary folder paths
    private Map<String, Transformation> transformations; // Image transformations

    @Getter
    @Setter
    public static class Dimensions {
        private int width;
        private int height;
    }

    @Getter
    @Setter
    public static class Transformation {
        private int width;
        private int height;
        private String crop;
        private String gravity; // Optional: for face detection (avatars)
    }
}
