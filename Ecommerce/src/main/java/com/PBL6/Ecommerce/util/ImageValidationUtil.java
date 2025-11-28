package com.PBL6.Ecommerce.util;

import com.PBL6.Ecommerce.config.ImageUploadConfig;
import com.PBL6.Ecommerce.exception.ImageValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Image Validation Utility
 * Validates image format, size, dimensions, and content-type
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ImageValidationUtil {

    private final ImageUploadConfig config;
    private final Tika tika = new Tika();

    /**
     * Validates uploaded image file against all constraints
     * 
     * @param file Multipart file to validate
     * @throws ImageValidationException if validation fails
     */
    public void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImageValidationException("Image file is required");
        }

        // Validate file size
        validateSize(file);
        
        // Validate content type using Tika (more reliable than file extension)
        validateContentType(file);
        
        // Validate image dimensions
        validateDimensions(file);
        
        log.debug("Image validation passed for file: {}", file.getOriginalFilename());
    }

    /**
     * Validates file size against max size constraint
     */
    private void validateSize(MultipartFile file) {
        if (file.getSize() > config.getMaxSize()) {
            throw new ImageValidationException(
                String.format("File size exceeds maximum allowed size of %d bytes (%.2f MB)",
                    config.getMaxSize(),
                    config.getMaxSize() / (1024.0 * 1024.0))
            );
        }
    }

    /**
     * Validates content type using Apache Tika (detects actual file type, not extension)
     */
    private void validateContentType(MultipartFile file) {
        try (InputStream stream = file.getInputStream()) {
            String detectedType = tika.detect(stream);
            
            // Extract format from MIME type (e.g., "image/jpeg" -> "jpeg")
            String format = detectedType.replace("image/", "").toLowerCase();
            
            // Normalize format (jpeg -> jpg)
            if ("jpeg".equals(format)) {
                format = "jpg";
            }
            
            List<String> allowedFormats = config.getAllowedFormats();
            if (!allowedFormats.contains(format)) {
                throw new ImageValidationException(
                    String.format("Invalid image format. Allowed formats: %s, detected: %s",
                        String.join(", ", allowedFormats), format)
                );
            }
        } catch (IOException e) {
            log.error("Failed to detect content type", e);
            throw new ImageValidationException("Failed to validate image content type", e);
        }
    }

    /**
     * Validates image dimensions (width and height)
     */
    private void validateDimensions(MultipartFile file) {
        try (InputStream stream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(stream);
            
            if (image == null) {
                throw new ImageValidationException("Failed to read image dimensions. File may be corrupted.");
            }

            int width = image.getWidth();
            int height = image.getHeight();

            // Validate minimum dimensions
            if (width < config.getMinDimensions().getWidth() || 
                height < config.getMinDimensions().getHeight()) {
                throw new ImageValidationException(
                    String.format("Image dimensions too small. Minimum: %dx%d, actual: %dx%d",
                        config.getMinDimensions().getWidth(),
                        config.getMinDimensions().getHeight(),
                        width, height)
                );
            }

            // Validate maximum dimensions
            if (width > config.getMaxDimensions().getWidth() || 
                height > config.getMaxDimensions().getHeight()) {
                throw new ImageValidationException(
                    String.format("Image dimensions too large. Maximum: %dx%d, actual: %dx%d",
                        config.getMaxDimensions().getWidth(),
                        config.getMaxDimensions().getHeight(),
                        width, height)
                );
            }

            log.debug("Image dimensions validated: {}x{}", width, height);
        } catch (IOException e) {
            log.error("Failed to read image dimensions", e);
            throw new ImageValidationException("Failed to validate image dimensions", e);
        }
    }

    /**
     * Checks if file has valid image extension (basic check)
     */
    public boolean hasValidImageExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return config.getAllowedFormats().contains(extension);
    }
}
