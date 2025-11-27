package com.PBL6.Ecommerce.service.impl;

import com.PBL6.Ecommerce.domain.Product;
import com.PBL6.Ecommerce.dto.response.ImageUploadResponse;
import com.PBL6.Ecommerce.exception.*;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.service.ProductImageService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Implementation of ProductImageService for managing product images
 * Uses Cloudinary for cloud storage and Apache Tika for MIME validation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductRepository productRepository;
    private final Cloudinary cloudinary;
    private final Tika tika = new Tika();

    @Value("${image.upload.max-size:5242880}") // 5MB default
    private long maxFileSize;

    @Value("${image.upload.allowed-formats:jpg,jpeg,png,webp}")
    private String allowedFormats;

    @Value("${image.upload.min-dimensions.width:500}")
    private int minWidth;

    @Value("${image.upload.min-dimensions.height:500}")
    private int minHeight;

    @Value("${image.upload.max-dimensions.width:4000}")
    private int maxWidth;

    @Value("${image.upload.max-dimensions.height:4000}")
    private int maxHeight;

    @Override
    @Transactional
    public ImageUploadResponse uploadMainImage(Long productId, MultipartFile file, Long userId) {
        log.info("Uploading main image for product {} by user {}", productId, userId);

        // 1. Validate product exists and seller owns it
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Product {} not found", productId);
                    return new ProductNotFoundException("Product with ID " + productId + " not found");
                });

        if (!product.getShop().getOwner().getId().equals(userId)) {
            log.error("User {} is not authorized to modify product {}", userId, productId);
            throw new UnauthorizedProductAccessException("You are not authorized to modify this product");
        }

        // 2. Validate image file
        validateImage(file);

        // 3. Store old image info for cleanup on success
        String oldPublicId = product.getMainImagePublicId();

        // 4. Upload to Cloudinary
        Map<String, Object> uploadResult;
        try {
            uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "products/" + productId + "/main",
                            "resource_type", "image",
                            "overwrite", false,
                            "unique_filename", true
                    ));
            log.info("Image uploaded to Cloudinary successfully: {}", uploadResult.get("public_id"));
        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary for product {}: {}", productId, e.getMessage(), e);
            throw new ImageUploadException("Failed to upload image to cloud storage: " + e.getMessage());
        }

        String imageUrl = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");
        Integer width = (Integer) uploadResult.get("width");
        Integer height = (Integer) uploadResult.get("height");

        // 5. Validate uploaded image dimensions
        if (width < minWidth || height < minHeight) {
            // Rollback: Delete uploaded image from Cloudinary
            deleteFromCloudinary(publicId);
            throw new ImageValidationException(
                    String.format("Image dimensions %dx%d are below minimum required %dx%d",
                            width, height, minWidth, minHeight));
        }

        if (width > maxWidth || height > maxHeight) {
            // Rollback: Delete uploaded image from Cloudinary
            deleteFromCloudinary(publicId);
            throw new ImageValidationException(
                    String.format("Image dimensions %dx%d exceed maximum allowed %dx%d",
                            width, height, maxWidth, maxHeight));
        }

        // 6. Update product with new image (optimistic locking)
        try {
            product.setMainImage(imageUrl);
            product.setMainImagePublicId(publicId);
            productRepository.save(product);
            log.info("Product {} main image updated successfully", productId);
        } catch (OptimisticLockingFailureException e) {
            // Rollback: Delete uploaded image from Cloudinary
            deleteFromCloudinary(publicId);
            log.warn("Product {} was modified by another user during image upload. userId={}", productId, userId);
            throw new ImageUploadException("Product was modified by another user, please retry");
        } catch (Exception e) {
            // Rollback: Delete uploaded image from Cloudinary
            deleteFromCloudinary(publicId);
            log.error("Failed to save product {} with new main image: {}", productId, e.getMessage(), e);
            throw new ImageUploadException("Failed to update product with new image");
        }

        // 7. Delete old image from Cloudinary (after successful DB update)
        if (oldPublicId != null && !oldPublicId.isEmpty()) {
            deleteFromCloudinary(oldPublicId);
            log.info("Old main image deleted from Cloudinary: {}", oldPublicId);
        }

        // 8. Return response
        return ImageUploadResponse.builder()
                .url(imageUrl)
                .publicId(publicId)
                .width(width)
                .height(height)
                .size(file.getSize())
                .message("Main product image uploaded successfully")
                .build();
    }

    /**
     * Validate image file format, size, and MIME type
     */
    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImageValidationException("Image file cannot be empty");
        }

        // Validate file size
        if (file.getSize() > maxFileSize) {
            throw new ImageValidationException(
                    String.format("File size %d bytes exceeds maximum allowed %d bytes", 
                            file.getSize(), maxFileSize));
        }

        // Validate MIME type using Apache Tika (magic bytes detection)
        try {
            String detectedMimeType = tika.detect(file.getBytes());
            if (detectedMimeType == null || !detectedMimeType.startsWith("image/")) {
                throw new ImageValidationException("File is not a valid image. Detected type: " + detectedMimeType);
            }

            // Validate specific formats
            String format = detectedMimeType.substring(6); // Remove "image/" prefix
            if (!allowedFormats.contains(format)) {
                throw new ImageValidationException(
                        String.format("Image format '%s' is not allowed. Allowed formats: %s",
                                format, allowedFormats));
            }

        } catch (IOException e) {
            log.error("Failed to detect MIME type: {}", e.getMessage());
            throw new ImageValidationException("Failed to validate image file");
        }
    }

    /**
     * Delete image from Cloudinary
     * Logs errors but doesn't throw exceptions (cleanup operation)
     */
    private void deleteFromCloudinary(String publicId) {
        if (publicId == null || publicId.isEmpty()) {
            return;
        }

        try {
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Deleted image from Cloudinary: {} - Result: {}", publicId, result.get("result"));
        } catch (Exception e) {
            log.error("Failed to delete image from Cloudinary: {} - Error: {}", publicId, e.getMessage());
            // Don't throw exception - this is cleanup operation
        }
    }
}
