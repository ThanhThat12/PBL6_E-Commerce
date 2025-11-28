package com.PBL6.Ecommerce.util;

import org.springframework.stereotype.Component;

/**
 * Image URL Utility
 * Generates placeholder images and parses image URLs
 */
@Component
public class ImageUrlUtil {

    private static final String DEFAULT_PRODUCT_PLACEHOLDER = "https://via.placeholder.com/600x600/CCCCCC/666666?text=No+Image";
    private static final String DEFAULT_AVATAR_PLACEHOLDER = "https://via.placeholder.com/300x300/CCCCCC/666666?text=No+Avatar";
    private static final String DEFAULT_LOGO_PLACEHOLDER = "https://via.placeholder.com/400x400/CCCCCC/666666?text=No+Logo";
    private static final String DEFAULT_BANNER_PLACEHOLDER = "https://via.placeholder.com/1200x400/CCCCCC/666666?text=No+Banner";

    /**
     * Returns placeholder URL for product images
     */
    public String getProductPlaceholder() {
        return DEFAULT_PRODUCT_PLACEHOLDER;
    }

    /**
     * Returns placeholder URL for user avatars
     */
    public String getAvatarPlaceholder() {
        return DEFAULT_AVATAR_PLACEHOLDER;
    }

    /**
     * Returns placeholder URL for shop logos
     */
    public String getLogoPlaceholder() {
        return DEFAULT_LOGO_PLACEHOLDER;
    }

    /**
     * Returns placeholder URL for shop banners
     */
    public String getBannerPlaceholder() {
        return DEFAULT_BANNER_PLACEHOLDER;
    }

    /**
     * Checks if URL is a placeholder
     */
    public boolean isPlaceholder(String url) {
        if (url == null || url.isEmpty()) {
            return true;
        }
        return url.contains("placeholder.com") || url.contains("via.placeholder");
    }

    /**
     * Validates if URL is a valid Cloudinary URL
     */
    public boolean isCloudinaryUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return url.contains("cloudinary.com") && url.startsWith("https://");
    }

    /**
     * Sanitizes filename for safe storage
     */
    public String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "image";
        }
        // Remove special characters and spaces
        return filename.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}
