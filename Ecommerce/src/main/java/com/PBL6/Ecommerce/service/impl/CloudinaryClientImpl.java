package com.PBL6.Ecommerce.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.PBL6.Ecommerce.constant.TransformationType;
import com.PBL6.Ecommerce.domain.dto.image.cloudinary.CloudinaryUploadResult;
import com.PBL6.Ecommerce.exception.CloudinaryServiceException;
import com.PBL6.Ecommerce.exception.ImageUploadException;
import com.PBL6.Ecommerce.service.CloudinaryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Cloudinary Client Implementation
 * Wraps Cloudinary SDK with retry logic and error handling
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryClientImpl implements CloudinaryClient {

    private final Cloudinary cloudinary;

    /**
     * Uploads image to Cloudinary with retry logic
     * Retries up to 3 times with exponential backoff on failure
     */
    @Override
    @Retryable(
        retryFor = {CloudinaryServiceException.class, IOException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CloudinaryUploadResult uploadImage(MultipartFile file, String folder, String publicId) {
        try {
            log.debug("Uploading image to Cloudinary: folder={}, publicId={}", folder, publicId);

            // Generate unique public ID if not provided
            if (publicId == null || publicId.isEmpty()) {
                publicId = UUID.randomUUID().toString();
            }

            // Build upload options
            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                "folder", folder,
                "public_id", publicId,
                "resource_type", "image",
                "overwrite", false,
                "quality", "auto:good",
                "fetch_format", "auto"
            );

            // Upload to Cloudinary
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadOptions);

            // Map result to DTO
            CloudinaryUploadResult result = new CloudinaryUploadResult();
            result.setPublicId((String) uploadResult.get("public_id"));
            result.setUrl((String) uploadResult.get("url"));
            result.setSecureUrl((String) uploadResult.get("secure_url"));
            result.setFormat((String) uploadResult.get("format"));
            result.setWidth((Integer) uploadResult.get("width"));
            result.setHeight((Integer) uploadResult.get("height"));
            result.setBytes(((Number) uploadResult.get("bytes")).longValue());
            result.setResourceType((String) uploadResult.get("resource_type"));

            log.info("Image uploaded successfully: publicId={}, url={}", result.getPublicId(), result.getSecureUrl());
            return result;

        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new ImageUploadException("Failed to upload image: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during Cloudinary upload", e);
            throw new CloudinaryServiceException("Cloudinary service error: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes image from Cloudinary with retry logic
     */
    @Override
    @Retryable(
        retryFor = {CloudinaryServiceException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public boolean deleteImage(String publicId) {
        try {
            log.debug("Deleting image from Cloudinary: publicId={}", publicId);

            Map<?, ?> deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String result = (String) deleteResult.get("result");

            boolean success = "ok".equals(result);
            if (success) {
                log.info("Image deleted successfully: publicId={}", publicId);
            } else {
                log.warn("Image deletion returned non-ok result: publicId={}, result={}", publicId, result);
            }

            return success;

        } catch (Exception e) {
            log.error("Failed to delete image from Cloudinary: publicId={}", publicId, e);
            throw new CloudinaryServiceException("Failed to delete image: " + e.getMessage(), e);
        }
    }

    /**
     * Destroys image from Cloudinary (hard delete)
     */
    @Override
    @Retryable(
        retryFor = {CloudinaryServiceException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public Map<String, Object> destroyImage(String publicId) {
        try {
            log.debug("Destroying image from Cloudinary: publicId={}", publicId);

            Map<?, ?> destroyResult = cloudinary.uploader().destroy(publicId, 
                ObjectUtils.asMap("invalidate", true));
            
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) destroyResult;
            
            log.info("Image destroyed: publicId={}, result={}", publicId, result.get("result"));
            return result;

        } catch (Exception e) {
            log.error("Failed to destroy image from Cloudinary: publicId={}", publicId, e);
            throw new CloudinaryServiceException("Failed to destroy image: " + e.getMessage(), e);
        }
    }

    /**
     * Generates transformation URL for existing image
     */
    @Override
    public String generateTransformationUrl(String publicId, String transformation) {
        if (publicId == null || publicId.isEmpty()) {
            return null;
        }

        try {
            // Build transformation URL using Cloudinary SDK
            // Create a new Transformation object and apply the transformation string
            com.cloudinary.Transformation t = new com.cloudinary.Transformation();
            t.rawTransformation(transformation);
            
            String url = cloudinary.url()
                .transformation(t)
                .secure(true)
                .generate(publicId);

            log.debug("Generated transformation URL: publicId={}, transformation={}, url={}", 
                publicId, transformation, url);
            
            return url;

        } catch (Exception e) {
            log.error("Failed to generate transformation URL: publicId={}, transformation={}", 
                publicId, transformation, e);
            return null;
        }
    }

    @Override
    public Map<TransformationType, String> generateTransformedUrls(String publicId, String format) {
        if (publicId == null || publicId.isEmpty()) {
            return new EnumMap<>(TransformationType.class);
        }
        Map<TransformationType, String> transformedUrls = new EnumMap<>(TransformationType.class);

        try {
            // Thumbnail: 200x200 (fill)
            String thumbUrl = cloudinary.url().transformation(new com.cloudinary.Transformation<>()
                .width(200).height(200).crop("fill")).secure(true).generate(publicId);
            transformedUrls.put(TransformationType.THUMBNAIL, thumbUrl);

            // Medium: 600x600 (fit)
            String mediumUrl = cloudinary.url().transformation(new com.cloudinary.Transformation<>()
                .width(600).height(600).crop("fit")).secure(true).generate(publicId);
            transformedUrls.put(TransformationType.MEDIUM, mediumUrl);

            // Large: 1200x1200 (fit)
            String largeUrl = cloudinary.url().transformation(new com.cloudinary.Transformation<>()
                .width(1200).height(1200).crop("fit")).secure(true).generate(publicId);
            transformedUrls.put(TransformationType.LARGE, largeUrl);
            
            // Original URL - without transformations
            String originalUrl = cloudinary.url().secure(true).generate(publicId);
            transformedUrls.put(TransformationType.ORIGINAL, originalUrl);
            
            log.debug("Generated transformed URLs for publicId: {}", publicId);
            return transformedUrls;

        } catch (Exception e) {
            log.error("Failed to generate transformed URLs for publicId: {}", publicId, e);
            // Return empty map or rethrow as a custom exception
            return new EnumMap<>(TransformationType.class);
        }
    }
}
