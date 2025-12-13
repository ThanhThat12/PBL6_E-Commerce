package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.constant.TransformationType;
import com.PBL6.Ecommerce.domain.entity.product.Product;
import com.PBL6.Ecommerce.domain.entity.product.ProductImage;
import com.PBL6.Ecommerce.domain.entity.product.ProductPrimaryAttribute;
import com.PBL6.Ecommerce.domain.entity.product.ProductReview;
import com.PBL6.Ecommerce.domain.entity.product.ProductVariant;
import com.PBL6.Ecommerce.domain.entity.product.ProductVariantValue;
import com.PBL6.Ecommerce.domain.entity.shop.Shop;
import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.domain.dto.TempImageUploadResponseDTO;
import com.PBL6.Ecommerce.domain.dto.image.cloudinary.CloudinaryUploadResult;
import com.PBL6.Ecommerce.domain.dto.image.response.GalleryImageResponse;
import com.PBL6.Ecommerce.domain.dto.request.ImageReorderRequest;
import com.PBL6.Ecommerce.domain.dto.response.ImageDeleteResponse;
import com.PBL6.Ecommerce.domain.dto.response.ImageTransformationResponse;
import com.PBL6.Ecommerce.domain.dto.response.ImageUploadResponse;
import com.PBL6.Ecommerce.domain.dto.response.PrimaryAttributeDTO;
import com.PBL6.Ecommerce.domain.dto.response.ProductImageResponse;
import com.PBL6.Ecommerce.domain.dto.response.ProductImagesResponse;
import com.PBL6.Ecommerce.domain.dto.response.VariantImageResponse;

import com.PBL6.Ecommerce.domain.dto.request.ImageUploadRequest;
import com.PBL6.Ecommerce.exception.BusinessException;
import com.PBL6.Ecommerce.exception.CloudinaryServiceException;
import com.PBL6.Ecommerce.exception.ImageNotFoundException;
import com.PBL6.Ecommerce.exception.ImageUploadException;
import com.PBL6.Ecommerce.exception.ImageValidationException;
import com.PBL6.Ecommerce.repository.ProductAttributeRepository;
import com.PBL6.Ecommerce.repository.ProductImageRepository;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.repository.ProductReviewRepository;
import com.PBL6.Ecommerce.repository.ProductVariantRepository;
import com.PBL6.Ecommerce.repository.ProductVariantValueRepository;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.util.CloudinaryUtil;
import com.PBL6.Ecommerce.util.ImageValidationUtil;
import com.PBL6.Ecommerce.config.ImageUploadConfig;
import com.PBL6.Ecommerce.config.RateLimitConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final CloudinaryClient cloudinaryClient;
    private final ImageValidationUtil imageValidationUtil;
    private final CloudinaryUtil cloudinaryUtil;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final ProductReviewRepository productReviewRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductVariantValueRepository productVariantValueRepository;
    private final ProductAttributeRepository productAttributeRepository;
    private final com.PBL6.Ecommerce.repository.ProductPrimaryAttributeRepository productPrimaryAttributeRepository;
    private final ImageUploadConfig imageUploadConfig;
    private final RateLimitConfig rateLimitConfig;

    private static final int MAX_GALLERY_IMAGES = 10;
    private static final int MAX_REVIEW_IMAGES = 5;

    // ========== PRODUCT MAIN IMAGE ==========

    @Override
    @Transactional
    public ImageUploadResponse uploadProductMainImage(Long productId, MultipartFile file, Long userId) {
        log.info("Uploading main image for product {} by user {}", productId, userId);

        // Rate limiting
        checkRateLimit(userId);

        // Validate file
        imageValidationUtil.validateImage(file);

        // Find product and verify ownership
        Product product = findProductAndVerifyOwnership(productId, userId);

        // Delete old main image if exists
        if (product.getMainImagePublicId() != null) {
            try {
                cloudinaryClient.deleteImage(product.getMainImagePublicId());
                log.info("Deleted old main image: {}", product.getMainImagePublicId());
            } catch (Exception e) {
                log.warn("Failed to delete old main image, continuing with upload: {}", e.getMessage());
            }
        }

        // Upload to Cloudinary
        String folder = cloudinaryUtil.getFolderPath("product-main");
        String publicId = String.format("product_%d_main_%d", productId, System.currentTimeMillis());
        
        CloudinaryUploadResult result;
        try {
            result = cloudinaryClient.uploadImage(file, folder, publicId);
        } catch (Exception e) {
            log.error("Failed to upload main image for product {}: {}", productId, e.getMessage(), e);
            throw new ImageUploadException("Failed to upload main image: " + e.getMessage(), e);
        }

        // Update product entity with optimistic locking support
        try {
            product.setMainImage(result.getSecureUrl());
            product.setMainImagePublicId(result.getPublicId());
            productRepository.save(product);
            log.info("Product {} main image updated successfully", productId);
        } catch (org.springframework.dao.OptimisticLockingFailureException e) {
            // Rollback: Delete uploaded image from Cloudinary
            try {
                cloudinaryClient.deleteImage(result.getPublicId());
                log.info("Rolled back uploaded image: {}", result.getPublicId());
            } catch (Exception deleteEx) {
                log.error("Failed to rollback uploaded image: {}", result.getPublicId(), deleteEx);
            }
            log.warn("Product {} was modified by another user during image upload. userId={}", productId, userId);
            throw new ImageUploadException("Product was modified by another user, please retry");
        } catch (Exception e) {
            // Rollback: Delete uploaded image from Cloudinary
            try {
                cloudinaryClient.deleteImage(result.getPublicId());
                log.info("Rolled back uploaded image due to error: {}", result.getPublicId());
            } catch (Exception deleteEx) {
                log.error("Failed to rollback uploaded image: {}", result.getPublicId(), deleteEx);
            }
            log.error("Failed to save product {} with new main image: {}", productId, e.getMessage(), e);
            throw new ImageUploadException("Failed to update product with new image");
        }

        // Build transformation URLs
        Map<TransformationType, String> transformations = cloudinaryUtil.buildAllTransformations(result.getPublicId());

        log.info("Successfully uploaded main image for product {}: {}", productId, result.getPublicId());

        return ImageUploadResponse.builder()
                .id(productId)
                .url(result.getSecureUrl())
                .publicId(result.getPublicId())
                .width(result.getWidth())
                .height(result.getHeight())
                .size(result.getBytes())
                .transformations(transformations)
                .message("Main image uploaded successfully")
                .build();
    }

    @Override
    @Transactional
    public ImageDeleteResponse deleteProductMainImage(Long productId, Long userId) {
        log.info("Deleting main image for product {} by user {}", productId, userId);

        // Find product and verify ownership
        Product product = findProductAndVerifyOwnership(productId, userId);

        // Check if main image exists
        if (product.getMainImagePublicId() == null) {
            throw new ImageNotFoundException("Product does not have a main image");
        }

        String publicId = product.getMainImagePublicId();

        // Delete from Cloudinary
        try {
            boolean deleted = cloudinaryClient.deleteImage(publicId);
            if (!deleted) {
                log.warn("Cloudinary reported image not deleted: {}", publicId);
            }
        } catch (Exception e) {
            log.error("Failed to delete main image from Cloudinary: {}", e.getMessage(), e);
            throw new CloudinaryServiceException("Failed to delete image from Cloudinary: " + e.getMessage(), e);
        }

        // Update product entity
        product.setMainImage(null);
        product.setMainImagePublicId(null);
        productRepository.save(product);

        log.info("Successfully deleted main image for product {}: {}", productId, publicId);

        return ImageDeleteResponse.builder()
                .success(true)
                .message("Main image deleted successfully")
                .deletedPublicId(publicId)
                .build();
    }

    // ========== PRODUCT GALLERY IMAGES ==========

    @Override
    @Transactional
    public List<ImageUploadResponse> uploadProductGalleryImages(Long productId, List<MultipartFile> files, Long userId, Long variantId) {
        log.info("Uploading {} gallery images for product {} (variant: {}) by user {}", files.size(), productId, variantId, userId);

        // Rate limiting
        checkRateLimit(userId);

        // Validate file count
        if (files == null || files.isEmpty()) {
            throw new ImageValidationException("At least one image file is required");
        }
        if (files.size() > MAX_GALLERY_IMAGES) {
            throw new ImageValidationException("Maximum " + MAX_GALLERY_IMAGES + " images allowed per upload");
        }

        // Find product and verify ownership
        Product product = findProductAndVerifyOwnership(productId, userId);

        // Validate variant if provided
        if (variantId != null) {
            ProductVariant variant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> new ImageValidationException("Variant not found with id: " + variantId));
            
            // Verify variant belongs to the product
            if (!variant.getProduct().getId().equals(productId)) {
                throw new ImageValidationException("Variant " + variantId + " does not belong to product " + productId);
            }
            
            log.debug("Validated variant {} for product {}", variantId, productId);
        }

        // Check total gallery image count
        Long currentImageCount = productImageRepository.countByProductId(productId);
        if (currentImageCount + files.size() > MAX_GALLERY_IMAGES) {
            throw new ImageValidationException(
                String.format("Product already has %d images. Maximum %d images allowed per product", 
                              currentImageCount, MAX_GALLERY_IMAGES)
            );
        }

        // Get next display order
        Integer maxDisplayOrder = productImageRepository.findMaxDisplayOrderByProductId(productId);
        int nextDisplayOrder = (maxDisplayOrder == null) ? 0 : maxDisplayOrder + 1;

        List<ImageUploadResponse> responses = new ArrayList<>();
        String folder = cloudinaryUtil.getFolderPath("product-gallery");

        // Upload images in sequence (consider parallel for performance)
        for (MultipartFile file : files) {
            try {
                // Validate file
                imageValidationUtil.validateImage(file);

                // Upload to Cloudinary
                String publicId = String.format("product_%d_gallery_%d", productId, System.currentTimeMillis());
                CloudinaryUploadResult result = cloudinaryClient.uploadImage(file, folder, publicId);

                // Create ProductImage entity
                ProductImage productImage = new ProductImage();
                productImage.setProduct(product);
                productImage.setImageType(variantId != null ? "VARIANT" : "GALLERY");
                productImage.setImageUrl(result.getSecureUrl());
                productImage.setPublicId(result.getPublicId());
                productImage.setDisplayOrder(nextDisplayOrder++);
                productImage.setUploadedAt(java.time.LocalDateTime.now());

                try {
                    productImageRepository.save(productImage);
                } catch (org.springframework.dao.OptimisticLockingFailureException e) {
                    // Rollback: Delete uploaded images
                    log.warn("Optimistic locking failure during gallery upload for product {}", productId);
                    for (ImageUploadResponse resp : responses) {
                        try {
                            cloudinaryClient.deleteImage(resp.getPublicId());
                        } catch (Exception deleteEx) {
                            log.error("Failed to rollback image: {}", resp.getPublicId(), deleteEx);
                        }
                    }
                    // Delete current image
                    try {
                        cloudinaryClient.deleteImage(result.getPublicId());
                    } catch (Exception deleteEx) {
                        log.error("Failed to rollback current image: {}", result.getPublicId(), deleteEx);
                    }
                    throw new ImageUploadException("Product was modified during upload, please retry");
                }
                
                if (variantId != null) {
                    log.debug("Associated image {} with variant {}", productImage.getId(), variantId);
                }

                // Build transformation URLs
                Map<TransformationType, String> transformations = cloudinaryUtil.buildAllTransformations(result.getPublicId());

                responses.add(ImageUploadResponse.builder()
                        .id(productImage.getId())
                        .url(result.getSecureUrl())
                        .publicId(result.getPublicId())
                        .width(result.getWidth())
                        .height(result.getHeight())
                        .size(result.getBytes())
                        .transformations(transformations)
                        .message("Gallery image uploaded successfully")
                        .build());

                log.debug("Uploaded gallery image {}: {}", productImage.getId(), result.getPublicId());

            } catch (ImageValidationException e) {
                log.error("Validation failed for gallery image: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("Failed to upload gallery image: {}", e.getMessage(), e);
                throw new ImageUploadException("Failed to upload gallery image: " + e.getMessage(), e);
            }
        }

        log.info("Successfully uploaded {} gallery images for product {}", responses.size(), productId);
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductImageResponse> getProductGalleryImages(Long productId, String variantAttributeValue) {
        log.debug("Retrieving gallery images for product {} (variantAttributeValue: {})", productId, variantAttributeValue);

        List<ProductImage> images;
        if (variantAttributeValue != null && !variantAttributeValue.isEmpty()) {
            // Get images for specific variant attribute value
            images = productImageRepository.findByProductIdAndVariantAttributeValue(productId, variantAttributeValue);
        } else {
            // Get only GALLERY images (not VARIANT images)
            images = productImageRepository.findGalleryImagesByProductId(productId);
        }

        return images.stream()
                .map(this::mapToProductImageResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void reorderProductGalleryImages(Long productId, ImageReorderRequest request, Long userId) {
        log.info("Reordering {} gallery images for product {} by user {}", 
                 request.getImageOrders().size(), productId, userId);

        // Verify ownership
        findProductAndVerifyOwnership(productId, userId);

        // Validate and fetch images
        List<ImageReorderRequest.ImageOrderItem> imageOrders = request.getImageOrders();
        if (imageOrders == null || imageOrders.isEmpty()) {
            throw new ImageValidationException("Image orders list cannot be empty");
        }

        // Update display order
        for (ImageReorderRequest.ImageOrderItem orderItem : imageOrders) {
            Long imageId = orderItem.getImageId();
            Integer displayOrder = orderItem.getDisplayOrder();
            
            ProductImage image = productImageRepository.findById(imageId)
                    .orElseThrow(() -> new ImageNotFoundException("Image not found: " + imageId));

            // Verify image belongs to the product
            if (!image.getProduct().getId().equals(productId)) {
                throw new ImageValidationException("Image " + imageId + " does not belong to product " + productId);
            }

            image.setDisplayOrder(displayOrder);
            // image.setUpdatedAt(LocalDateTime.now());
            productImageRepository.save(image);
        }

        log.info("Successfully reordered gallery images for product {}", productId);
    }

    @Override
    @Transactional
    public ImageDeleteResponse deleteProductGalleryImage(Long productId, Long imageId, Long userId) {
        log.info("Deleting gallery image {} for product {} by user {}", imageId, productId, userId);

        // Verify ownership
        findProductAndVerifyOwnership(productId, userId);

        // Find image
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException("Image not found: " + imageId));

        // Verify image belongs to the product
        if (!image.getProduct().getId().equals(productId)) {
            throw new ImageValidationException("Image does not belong to this product");
        }

        String publicId = image.getPublicId();

        // Delete from Cloudinary
        try {
            boolean deleted = cloudinaryClient.deleteImage(publicId);
            if (!deleted) {
                log.warn("Cloudinary reported image not deleted: {}", publicId);
            }
        } catch (Exception e) {
            log.error("Failed to delete gallery image from Cloudinary: {}", e.getMessage(), e);
            throw new CloudinaryServiceException("Failed to delete image from Cloudinary: " + e.getMessage(), e);
        }

        // Delete from database
        productImageRepository.delete(image);

        log.info("Successfully deleted gallery image {}: {}", imageId, publicId);

        return ImageDeleteResponse.builder()
                .success(true)
                .message("Gallery image deleted successfully")
                .deletedPublicId(publicId)
                .build();
    }

    // ========== BATCH VARIANT IMAGE UPLOAD ==========

    @Override
    @Transactional
    public Map<String, List<ImageUploadResponse>> uploadBatchVariantImages(
            Long productId,
            List<MultipartFile> files,
            Map<String, List<Integer>> variantMappings,
            Long userId) {
        
        log.info("Batch uploading variant images for product {} by user {}. Mappings: {}", 
                 productId, userId, variantMappings);

        // Rate limiting
        checkRateLimit(userId);

        // Verify ownership
        Product product = findProductAndVerifyOwnership(productId, userId);

        // Validate request
        if (files.isEmpty()) {
            throw new ImageValidationException("No files provided for upload");
        }
        if (variantMappings == null || variantMappings.isEmpty()) {
            throw new ImageValidationException("No variant mappings provided");
        }

        // Validate file indices
        for (Map.Entry<String, List<Integer>> entry : variantMappings.entrySet()) {
            String variantName = entry.getKey();
            List<Integer> indices = entry.getValue();
            
            if (indices == null || indices.isEmpty()) {
                throw new ImageValidationException("No file indices provided for variant: " + variantName);
            }
            
            // NEW: Enforce exactly 1 image per variant value
            if (indices.size() != 1) {
                throw new ImageValidationException(
                    String.format("Variant value '%s' must have exactly 1 image. Found: %d images",
                                variantName, indices.size()));
            }
            
            for (Integer index : indices) {
                if (index < 0 || index >= files.size()) {
                    throw new ImageValidationException(
                        String.format("Invalid file index %d for variant %s. Must be between 0 and %d",
                                    index, variantName, files.size() - 1));
                }
            }
        }

        // Validate total images don't exceed max
        int totalImages = variantMappings.values().stream()
                .mapToInt(List::size)
                .sum();
        if (totalImages > MAX_GALLERY_IMAGES) {
            throw new ImageValidationException(
                String.format("Total images (%d) exceeds maximum allowed (%d)", 
                            totalImages, MAX_GALLERY_IMAGES));
        }

        // Validate all files
        for (MultipartFile file : files) {
            imageValidationUtil.validateImage(file);
        }

        // Get all variants for product
        List<ProductVariant> allVariants = productVariantRepository.findByProductId(productId);
        if (allVariants.isEmpty()) {
            throw new ImageValidationException("Product has no variants");
        }

        // Process each variant value name and upload its image (exactly 1 per value)
        Map<String, List<ImageUploadResponse>> results = new HashMap<>();
        
        for (Map.Entry<String, List<Integer>> entry : variantMappings.entrySet()) {
            String variantValueName = entry.getKey();
            List<Integer> fileIndices = entry.getValue();
            
            log.info("Processing variant value: {} with {} image", variantValueName, fileIndices.size());
            
            // NEW: Use direct query to find ProductVariantValue (Group 1)
            ProductVariantValue variantValue = productVariantValueRepository
                .findByProductIdAndValueName(productId, variantValueName)
                .orElseThrow(() -> new ImageValidationException(
                    String.format("No variant value found with name '%s' for product %d", 
                                variantValueName, productId)));
            
            // Get first variant with this value for variant_id association
            ProductVariant variant = variantValue.getVariant();
            
            log.info("Found variant value '{}' (ID: {}) for product {}. Using variant ID: {} for image association",
                    variantValueName, variantValue.getId(), productId, variant.getId());

            // Upload the single image for this variant value
            List<ImageUploadResponse> uploadedImages = new ArrayList<>();
            
            Integer fileIndex = fileIndices.get(0); // Exactly 1 image per variant value
            MultipartFile file = files.get(fileIndex);
            
            // Upload to Cloudinary
            String folder = cloudinaryUtil.getFolderPath("product-gallery");
            String publicId = String.format("product_%d_variant_%s_%d", 
                                          productId, variantValueName, System.currentTimeMillis());
            
            CloudinaryUploadResult result;
            try {
                result = cloudinaryClient.uploadImage(file, folder, publicId);
            } catch (Exception e) {
                log.error("Failed to upload image for variant {}: {}", variantValueName, e.getMessage(), e);
                throw new ImageUploadException(
                    String.format("Failed to upload image for variant %s: %s", 
                                variantValueName, e.getMessage()), e);
            }

            // Save to database with variant attribute info
            ProductImage productImage = new ProductImage();
            productImage.setProduct(product);
            productImage.setImageUrl(result.getUrl());
            productImage.setPublicId(result.getPublicId());
            productImage.setImageType("VARIANT"); // Mark as variant-specific image
            productImage.setVariantAttributeValue(variantValueName); // Display-friendly name (e.g., "Red")
            productImage.setDisplayOrder(0); // Single image per variant value
            // Note: variantAttribute FK removed - only using variantAttributeValue string now
            productImage.setUploadedAt(java.time.LocalDateTime.now());
            
            productImageRepository.save(productImage);

            // Build response
            ImageUploadResponse response = ImageUploadResponse.builder()
                    .url(result.getUrl())
                    .publicId(result.getPublicId())
                    .width(result.getWidth())
                    .height(result.getHeight())
                    .size(result.getBytes())
                    .transformations(cloudinaryUtil.buildAllTransformations(result.getPublicId()))
                    .build();
            
            uploadedImages.add(response);
            
            log.info("Uploaded image for variant '{}': {}", variantValueName, result.getPublicId());
            
            results.put(variantValueName, uploadedImages);
        }

        log.info("Successfully batch uploaded variant images for product {}. Total variants: {}, Total images: {}",
                productId, results.size(), results.size()); // Total images = total variants (1:1)

        return results;
    }

    // ========== HELPER METHODS ==========

    /**
     * Find product by ID and verify ownership by user
     */
    private Product findProductAndVerifyOwnership(Long productId, Long userId) {
        // TEMPORARY DEBUG LOGGING
        log.info("--- DEBUG: Verifying ownership for productId: {}, userId: {}", productId, userId);
        List<Product> allProducts = productRepository.findAll();
        log.info("--- DEBUG: Found {} total products in the database.", allProducts.size());
        allProducts.forEach(p -> {
            if (p.getShop() != null && p.getShop().getOwner() != null) {
                log.info("--- DEBUG: DB Product: id={}, name={}, shop_owner_id={}", p.getId(), p.getName(), p.getShop().getOwner().getId());
            } else {
                log.info("--- DEBUG: DB Product: id={}, name={}, shop_owner_id=NULL", p.getId(), p.getName());
            }
        });
        // END TEMPORARY DEBUG LOGGING

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ImageNotFoundException("Product not found: " + productId));

        // Verify ownership (assuming product.shop.owner.id)
        if (!product.getShop().getOwner().getId().equals(userId)) {
            log.warn("User {} attempted to access product {} owned by user {}",
                     userId, productId, product.getShop().getOwner().getId());
            throw new SecurityException("You do not have permission to manage images for this product");
        }

        return product;
    }

    /**
     * Find shop by ID and verify ownership by user
     */
    private Shop findShopAndVerifyOwnership(Long shopId, Long userId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ImageNotFoundException("Shop not found: " + shopId));

        // Verify ownership (shop.owner.id)
        if (!shop.getOwner().getId().equals(userId)) {
            log.warn("User {} attempted to access shop {} owned by user {}",
                     userId, shopId, shop.getOwner().getId());
            throw new SecurityException("You do not have permission to manage images for this shop");
        }

        return shop;
    }

    /**
     * Check rate limit for user
     */
    private void checkRateLimit(Long userId) {
        Bucket bucket = rateLimitConfig.resolveBucket(userId);
        if (!bucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for user {}", userId);
            throw new ImageValidationException("Rate limit exceeded. Please try again later.");
        }
    }

    /**
     * Map ProductImage entity to ProductImageResponse DTO
     */
    private ProductImageResponse mapToProductImageResponse(ProductImage image) {
        Map<TransformationType, String> transformations = cloudinaryUtil.buildAllTransformations(image.getPublicId());

        ImageTransformationResponse transformationResponse = ImageTransformationResponse.builder()
                .thumbnail(transformations.get(TransformationType.THUMBNAIL))
                .medium(transformations.get(TransformationType.MEDIUM))
                .large(transformations.get(TransformationType.LARGE))
                .original(transformations.get(TransformationType.ORIGINAL))
                .build();

        return ProductImageResponse.builder()
                .id(image.getId())
                .url(image.getImageUrl())
                .publicId(image.getPublicId())
                .displayOrder(image.getDisplayOrder())
                .variantId(null) // variantAttribute field removed - no longer available
                .variantValueName(image.getVariantAttributeValue())
                .transformations(transformationResponse)
                .build();
    }

    // ========== USER AVATAR ==========

    @Override
    @Transactional
    public ImageUploadResponse uploadUserAvatar(Long userId, MultipartFile file) {
        log.info("Uploading avatar for user {}", userId);

        // Rate limiting
        checkRateLimit(userId);

        // Validate file (basic validation only - dimension validation may not be available)
        imageValidationUtil.validateImage(file);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Delete old avatar from Cloudinary if exists
        if (user.getAvatarPublicId() != null) {
            try {
                cloudinaryClient.deleteImage(user.getAvatarPublicId());
                log.info("Deleted old avatar for user {}: {}", userId, user.getAvatarPublicId());
            } catch (Exception e) {
                log.warn("Failed to delete old avatar for user {}: {}", userId, e.getMessage());
                // Continue with upload even if deletion fails
            }
        }

        // Upload to Cloudinary
        String folder = cloudinaryUtil.getFolderPath("user-avatar");
        String publicId = String.format("user_%d_avatar_%d", userId, System.currentTimeMillis());

        CloudinaryUploadResult result;
        try {
            result = cloudinaryClient.uploadImage(file, folder, publicId);
        } catch (Exception e) {
            log.error("Failed to upload avatar for user {}: {}", userId, e.getMessage(), e);
            throw new ImageUploadException("Failed to upload avatar: " + e.getMessage(), e);
        }

        // Get avatar transformation configuration (300x300 with face detection)
        ImageUploadConfig.Transformation avatarTransform = imageUploadConfig.getTransformations().get("avatar");
        String avatarTransformation = String.format("w_%d,h_%d,c_%s,g_%s", 
            avatarTransform.getWidth(), 
            avatarTransform.getHeight(), 
            avatarTransform.getCrop(), 
            avatarTransform.getGravity() != null ? avatarTransform.getGravity() : "center");
        
        String avatarUrl = cloudinaryClient.generateTransformationUrl(result.getPublicId(), avatarTransformation);

        // Update user entity
        user.setAvatarUrl(avatarUrl);
        user.setAvatarPublicId(result.getPublicId());
        userRepository.save(user);

        log.info("Avatar uploaded successfully for user {}: {}", userId, result.getPublicId());

        // Build transformation URLs
        Map<TransformationType, String> transformations = cloudinaryUtil.buildAllTransformations(result.getPublicId());

        return ImageUploadResponse.builder()
                .id(userId)
                .url(avatarUrl)
                .publicId(result.getPublicId())
                .width(result.getWidth())
                .height(result.getHeight())
                .size(result.getBytes())
                .transformations(transformations)
                .message("Avatar uploaded successfully")
                .build();
    }

    @Override
    @Transactional
    public ImageDeleteResponse deleteUserAvatar(Long userId) {
        log.info("Deleting avatar for user {}", userId);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Check if user has an avatar
        if (user.getAvatarPublicId() == null) {
            throw new ImageNotFoundException("User has no avatar to delete");
        }

        String publicId = user.getAvatarPublicId();

        // Delete from Cloudinary
        try {
            boolean deleted = cloudinaryClient.deleteImage(publicId);
            if (!deleted) {
                log.warn("Cloudinary reported avatar not deleted: {}", publicId);
            }
        } catch (Exception e) {
            log.error("Failed to delete avatar from Cloudinary for user {}: {}", userId, e.getMessage(), e);
            throw new CloudinaryServiceException("Failed to delete avatar from Cloudinary: " + e.getMessage(), e);
        }

        // Update user entity (set to null, placeholder will be handled by frontend)
        user.setAvatarUrl(null);
        user.setAvatarPublicId(null);
        userRepository.save(user);

        log.info("Avatar deleted successfully for user {}", userId);

        return ImageDeleteResponse.builder()
                .success(true)
                .message("Avatar deleted successfully")
                .deletedPublicId(publicId)
                .build();
    }

    // ========== SHOP BRANDING IMAGES ==========

    @Override
    @Transactional
    public ImageUploadResponse uploadShopLogo(Long shopId, MultipartFile file, Long userId) {
        log.info("Uploading logo for shop {} by user {}", shopId, userId);

        // Rate limiting
        checkRateLimit(userId);

        // Validate file
        imageValidationUtil.validateImage(file);

        // Find shop and verify ownership
        Shop shop = findShopAndVerifyOwnership(shopId, userId);

        // Delete old logo if exists
        if (shop.getLogoPublicId() != null) {
            try {
                cloudinaryClient.deleteImage(shop.getLogoPublicId());
                log.info("Deleted old logo for shop {}: {}", shopId, shop.getLogoPublicId());
            } catch (Exception e) {
                log.warn("Failed to delete old logo {} from Cloudinary: {}", shop.getLogoPublicId(), e.getMessage());
            }
        }

        // Upload to Cloudinary with logo transformation (400x400, preserve transparency)
        String folder = cloudinaryUtil.getFolderPath("shop-logo");
        String publicId = String.format("shop_%d_logo_%d", shopId, System.currentTimeMillis());

        CloudinaryUploadResult result;
        try {
            result = cloudinaryClient.uploadImage(file, folder, publicId);
        } catch (Exception e) {
            throw new ImageUploadException("Failed to upload shop logo: " + e.getMessage(), e);
        }

        // Update shop entity
        shop.setLogoUrl(result.getSecureUrl());
        shop.setLogoPublicId(result.getPublicId());
        shopRepository.save(shop);

        // Build transformation URLs (400x400 for logo)
        Map<TransformationType, String> transformations = cloudinaryUtil.buildAllTransformations(result.getPublicId());

        log.info("Successfully uploaded logo for shop {}: {}", shopId, result.getPublicId());

        return ImageUploadResponse.builder()
                .id(shopId)
                .url(result.getSecureUrl())
                .publicId(result.getPublicId())
                .width(result.getWidth())
                .height(result.getHeight())
                .size(result.getBytes())
                .transformations(transformations)
                .message("Shop logo uploaded successfully")
                .build();
    }

    @Override
    @Transactional
    public ImageUploadResponse uploadShopBanner(Long shopId, MultipartFile file, Long userId) {
        log.info("Uploading banner for shop {} by user {}", shopId, userId);

        // Rate limiting
        checkRateLimit(userId);

        // Validate file
        imageValidationUtil.validateImage(file);

        // Find shop and verify ownership
        Shop shop = findShopAndVerifyOwnership(shopId, userId);

        // Delete old banner if exists
        if (shop.getBannerPublicId() != null) {
            try {
                cloudinaryClient.deleteImage(shop.getBannerPublicId());
                log.info("Deleted old banner for shop {}: {}", shopId, shop.getBannerPublicId());
            } catch (Exception e) {
                log.warn("Failed to delete old banner {} from Cloudinary: {}", shop.getBannerPublicId(), e.getMessage());
            }
        }

        // Upload to Cloudinary with banner transformation (1200x400 crop fill)
        String folder = cloudinaryUtil.getFolderPath("shop-banner");
        String publicId = String.format("shop_%d_banner_%d", shopId, System.currentTimeMillis());

        CloudinaryUploadResult result;
        try {
            result = cloudinaryClient.uploadImage(file, folder, publicId);
        } catch (Exception e) {
            throw new ImageUploadException("Failed to upload shop banner: " + e.getMessage(), e);
        }

        // Update shop entity
        shop.setBannerUrl(result.getSecureUrl());
        shop.setBannerPublicId(result.getPublicId());
        shopRepository.save(shop);

        // Build transformation URLs
        Map<TransformationType, String> transformations = cloudinaryUtil.buildAllTransformations(result.getPublicId());

        log.info("Successfully uploaded banner for shop {}: {}", shopId, result.getPublicId());

        return ImageUploadResponse.builder()
                .id(shopId)
                .url(result.getSecureUrl())
                .publicId(result.getPublicId())
                .width(result.getWidth())
                .height(result.getHeight())
                .size(result.getBytes())
                .transformations(transformations)
                .message("Shop banner uploaded successfully")
                .build();
    }

    @Override
    @Transactional
    public ImageDeleteResponse deleteShopLogo(Long shopId, Long userId) {
        log.info("Deleting logo for shop {} by user {}", shopId, userId);

        // Find shop and verify ownership
        Shop shop = findShopAndVerifyOwnership(shopId, userId);

        // Check if logo exists
        if (shop.getLogoPublicId() == null) {
            throw new ImageNotFoundException("Shop has no logo to delete");
        }

        String publicId = shop.getLogoPublicId();

        // Delete from Cloudinary
        try {
            cloudinaryClient.deleteImage(publicId);
            log.info("Deleted logo from Cloudinary: {}", publicId);
        } catch (Exception e) {
            log.error("Failed to delete logo {} from Cloudinary: {}", publicId, e.getMessage());
            throw new CloudinaryServiceException("Failed to delete shop logo from Cloudinary", e);
        }

        // Update shop entity
        shop.setLogoUrl(null);
        shop.setLogoPublicId(null);
        shopRepository.save(shop);

        log.info("Logo deleted successfully for shop {}", shopId);

        return ImageDeleteResponse.builder()
                .success(true)
                .message("Shop logo deleted successfully")
                .deletedPublicId(publicId)
                .build();
    }

    @Override
    @Transactional
    public ImageDeleteResponse deleteShopBanner(Long shopId, Long userId) {
        log.info("Deleting banner for shop {} by user {}", shopId, userId);

        // Find shop and verify ownership
        Shop shop = findShopAndVerifyOwnership(shopId, userId);

        // Check if banner exists
        if (shop.getBannerPublicId() == null) {
            throw new ImageNotFoundException("Shop has no banner to delete");
        }

        String publicId = shop.getBannerPublicId();

        // Delete from Cloudinary
        try {
            cloudinaryClient.deleteImage(publicId);
            log.info("Deleted banner from Cloudinary: {}", publicId);
        } catch (Exception e) {
            log.error("Failed to delete banner {} from Cloudinary: {}", publicId, e.getMessage());
            throw new CloudinaryServiceException("Failed to delete shop banner from Cloudinary", e);
        }

        // Update shop entity
        shop.setBannerUrl(null);
        shop.setBannerPublicId(null);
        shopRepository.save(shop);

        log.info("Banner deleted successfully for shop {}", shopId);

        return ImageDeleteResponse.builder()
                .success(true)
                .message("Shop banner deleted successfully")
                .deletedPublicId(publicId)
                .build();
    }

    // ========== REVIEW IMAGES ==========

    @Override
    @Transactional
    public List<TempImageUploadResponseDTO> uploadTempReviewImages(List<MultipartFile> files, Long userId) {
        log.info("Uploading {} temporary review images for user {}", files.size(), userId);

        // Rate limiting
        checkRateLimit(userId);

        // Validate file count
        if (files == null || files.isEmpty()) {
            throw new ImageValidationException("At least one image file is required");
        }
        if (files.size() > MAX_REVIEW_IMAGES) {
            throw new ImageValidationException("Maximum " + MAX_REVIEW_IMAGES + " images allowed per review");
        }

        List<TempImageUploadResponseDTO> responses = new ArrayList<>();
        String folder = cloudinaryUtil.getFolderPath("review-temp");

        for (MultipartFile file : files) {
            try {
                // Validate file
                imageValidationUtil.validateImage(file);

                // Upload to Cloudinary with a temp prefix
                String publicId = String.format("temp_review_%d_%d", userId, System.currentTimeMillis());
                CloudinaryUploadResult result = cloudinaryClient.uploadImage(file, folder, publicId);

                TempImageUploadResponseDTO response = TempImageUploadResponseDTO.builder()
                        .url(result.getSecureUrl())
                        .publicId(result.getPublicId())
                        .width(result.getWidth())
                        .height(result.getHeight())
                        .size(result.getBytes())
                        .message("Image uploaded successfully")
                        .build();

                responses.add(response);

                log.debug("Uploaded temp review image: {} for user {}", result.getPublicId(), userId);

            } catch (ImageValidationException e) {
                throw e;
            } catch (Exception e) {
                log.error("Failed to upload temp review image for user {}: {}", userId, e.getMessage(), e);
                throw new ImageUploadException("Failed to upload image: " + e.getMessage());
            }
        }

        log.info("Successfully uploaded {} temporary review images for user {}", responses.size(), userId);
        return responses;
    }

    @Override
    @Transactional
    public List<ImageUploadResponse> uploadReviewImages(Long reviewId, List<MultipartFile> files, Long userId) {
        log.info("Uploading {} images for review {} by user {}", files.size(), reviewId, userId);

        // Rate limiting
        checkRateLimit(userId);

        // Validate file count
        if (files == null || files.isEmpty()) {
            throw new ImageValidationException("At least one image file is required");
        }
        if (files.size() > MAX_REVIEW_IMAGES) {
            throw new ImageValidationException("Maximum " + MAX_REVIEW_IMAGES + " images allowed per review");
        }

        // Find review and verify ownership
        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ImageNotFoundException("Review not found with id: " + reviewId));
        
        if (!review.getUser().getId().equals(userId)) {
            throw new SecurityException("User does not own this review");
        }

        // Parse existing images JSON
        List<Map<String, String>> existingImages = new ArrayList<>();
        if (review.getImages() != null && !review.getImages().isEmpty() && !review.getImages().equals("[]")) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                existingImages = mapper.readValue(review.getImages(), new TypeReference<List<Map<String, String>>>() {});
            } catch (Exception e) {
                log.warn("Failed to parse existing images JSON for review {}: {}", reviewId, e.getMessage());
            }
        }

        // Check total image count
        if (existingImages.size() + files.size() > MAX_REVIEW_IMAGES) {
            throw new ImageValidationException(
                String.format("Review already has %d images. Maximum %d images allowed per review", 
                              existingImages.size(), MAX_REVIEW_IMAGES)
            );
        }

        List<ImageUploadResponse> responses = new ArrayList<>();
        String folder = cloudinaryUtil.getFolderPath("review");

        // Upload images
        for (MultipartFile file : files) {
            try {
                // Validate file
                imageValidationUtil.validateImage(file);

                // Upload to Cloudinary
                String publicId = String.format("review_%d_image_%d", reviewId, System.currentTimeMillis());
                CloudinaryUploadResult result = cloudinaryClient.uploadImage(file, folder, publicId);

                // Add to existing images list
                Map<String, String> imageData = new HashMap<>();
                imageData.put("url", result.getSecureUrl());
                imageData.put("publicId", result.getPublicId());
                existingImages.add(imageData);

                // Build transformation URLs
                Map<TransformationType, String> transformations = cloudinaryUtil.buildAllTransformations(result.getPublicId());

                ImageUploadResponse response = ImageUploadResponse.builder()
                        .id((long) (existingImages.size() - 1)) // Use index as ID
                        .url(result.getSecureUrl())
                        .publicId(result.getPublicId())
                        .width(result.getWidth())
                        .height(result.getHeight())
                        .size(result.getBytes())
                        .transformations(transformations)
                        .message("Image uploaded successfully")
                        .build();

                responses.add(response);

                log.info("Uploaded image {} for review {}: {}", existingImages.size(), reviewId, result.getPublicId());

            } catch (Exception e) {
                log.error("Failed to upload image for review {}: {}", reviewId, e.getMessage(), e);
                throw new ImageUploadException("Failed to upload image: " + e.getMessage(), e);
            }
        }

        // Save updated images JSON
        try {
            ObjectMapper mapper = new ObjectMapper();
            String imagesJson = mapper.writeValueAsString(existingImages);
            review.setImages(imagesJson);
            review.setImagesCount(existingImages.size());
            productReviewRepository.save(review);
        } catch (Exception e) {
            log.error("Failed to save images JSON for review {}: {}", reviewId, e.getMessage(), e);
            throw new ImageUploadException("Failed to save images metadata: " + e.getMessage(), e);
        }

        log.info("Successfully uploaded {} images for review {}", responses.size(), reviewId);

        return responses;
    }

    @Override
    @Transactional
    public ImageDeleteResponse deleteReviewImage(Long reviewId, Integer imageIndex, Long userId) {
        log.info("Deleting image at index {} for review {} by user {}", imageIndex, reviewId, userId);

        // Find review and verify ownership
        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ImageNotFoundException("Review not found with id: " + reviewId));
        
        if (!review.getUser().getId().equals(userId)) {
            throw new SecurityException("User does not own this review");
        }

        // Check if review has images
        if (review.getImages() == null || review.getImages().isEmpty() || review.getImages().equals("[]")) {
            throw new ImageNotFoundException("Review has no images to delete");
        }

        // Parse images JSON
        List<Map<String, String>> images;
        try {
            ObjectMapper mapper = new ObjectMapper();
            images = mapper.readValue(review.getImages(), new TypeReference<List<Map<String, String>>>() {});
        } catch (Exception e) {
            log.error("Failed to parse images JSON for review {}: {}", reviewId, e.getMessage(), e);
            throw new ImageUploadException("Failed to parse images metadata: " + e.getMessage(), e);
        }

        // Validate index
        if (imageIndex < 0 || imageIndex >= images.size()) {
            throw new ImageNotFoundException("Invalid image index: " + imageIndex + ". Review has " + images.size() + " images");
        }

        // Get image to delete
        Map<String, String> imageToDelete = images.get(imageIndex);
        String publicId = imageToDelete.get("publicId");

        // Delete from Cloudinary
        try {
            boolean deleted = cloudinaryClient.deleteImage(publicId);
            if (!deleted) {
                log.warn("Cloudinary reported image not deleted: {}", publicId);
            }
        } catch (Exception e) {
            log.error("Failed to delete image from Cloudinary for review {}: {}", reviewId, e.getMessage(), e);
            throw new CloudinaryServiceException("Failed to delete image from Cloudinary: " + e.getMessage(), e);
        }

        // Remove from list
        images.remove(imageIndex.intValue());

        // Save updated images JSON
        try {
            ObjectMapper mapper = new ObjectMapper();
            String imagesJson = images.isEmpty() ? null : mapper.writeValueAsString(images);
            review.setImages(imagesJson);
            review.setImagesCount(images.size());
            productReviewRepository.save(review);
        } catch (Exception e) {
            log.error("Failed to save updated images JSON for review {}: {}", reviewId, e.getMessage(), e);
            throw new ImageUploadException("Failed to update images metadata: " + e.getMessage(), e);
        }

        log.info("Successfully deleted image at index {} for review {}: {}", imageIndex, reviewId, publicId);

        return ImageDeleteResponse.builder()
                .success(true)
                .message("Review image deleted successfully")
                .deletedPublicId(publicId)
                .build();
    }

    // ========== VARIANT-SPECIFIC IMAGES (Phase 5) ==========

    @Override
    @Transactional
    public VariantImageResponse uploadVariantImage(Long productId, MultipartFile file, String attributeValue, Long userId) {
        log.info("Uploading variant image for product {} with attribute value '{}' by user {}", 
                 productId, attributeValue, userId);

        // Rate limiting
        checkRateLimit(userId);

        // Validate file
        imageValidationUtil.validateImage(file);

        // Verify ownership
        Product product = findProductAndVerifyOwnership(productId, userId);

        // Verify product has primary attribute
        productPrimaryAttributeRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(
                    "Product does not have a primary attribute. Please set a primary attribute before uploading variant images."));

        // Validate attribute value exists for this product's primary attribute
        boolean valueExists = productVariantValueRepository.existsPrimaryAttributeValueByProductIdAndValue(
            productId, attributeValue);

        if (!valueExists) {
            // Get available values for better error message
            List<String> availableValues = productVariantValueRepository.findPrimaryAttributeValuesByProductId(productId);
            throw new ImageValidationException(
                String.format("Primary attribute value '%s' does not exist for product %d. Available values: %s", 
                    attributeValue, productId, availableValues));
        }

        // Check if variant image already exists (use simpler query to avoid duplicates)
        ProductImage existingImage = productImageRepository
                .findVariantImageByProductIdAndAttributeValue(productId, attributeValue)
                .orElse(null);

        String oldPublicId = null;
        if (existingImage != null) {
            oldPublicId = existingImage.getPublicId();
            log.info("Replacing existing variant image for '{}': {}", attributeValue, oldPublicId);
        }

        // Upload to Cloudinary
        String folder = cloudinaryUtil.getFolderPath("product-variants");
        String publicId = String.format("product_%d_variant_%s_%d", productId, attributeValue, System.currentTimeMillis());
        CloudinaryUploadResult result;
        
        try {
            result = cloudinaryClient.uploadImage(file, folder, publicId);
            log.info("Uploaded variant image to Cloudinary: {} (size: {} bytes)", result.getPublicId(), result.getBytes());
        } catch (Exception e) {
            log.error("Failed to upload variant image to Cloudinary: {}", e.getMessage(), e);
            throw new ImageUploadException("Failed to upload variant image: " + e.getMessage(), e);
        }

        // Save/Update ProductImage entity
        ProductImage productImage;
        if (existingImage != null) {
            productImage = existingImage;
            productImage.setImageUrl(result.getSecureUrl());
            productImage.setPublicId(result.getPublicId());
            productImage.setUploadedAt(LocalDateTime.now());
        } else {
            productImage = new ProductImage();
            productImage.setProduct(product);
            productImage.setImageType("VARIANT");
            productImage.setVariantAttributeValue(attributeValue);
            // Note: variantAttribute is not set for VARIANT type images as it can have multiple matching records
            productImage.setImageUrl(result.getSecureUrl());
            productImage.setPublicId(result.getPublicId());
            productImage.setDisplayOrder(0);
            productImage.setUploadedAt(LocalDateTime.now());
        }

        try {
            productImageRepository.save(productImage);
        } catch (Exception e) {
            // Rollback: delete uploaded image from Cloudinary
            log.warn("Database save failed, rolling back Cloudinary upload: {}", result.getPublicId());
            try {
                cloudinaryClient.deleteImage(result.getPublicId());
            } catch (Exception deleteEx) {
                log.error("Failed to rollback Cloudinary upload: {}", result.getPublicId(), deleteEx);
            }
            throw new ImageUploadException("Failed to save variant image to database: " + e.getMessage(), e);
        }

        // Delete old image from Cloudinary if it existed
        if (oldPublicId != null) {
            try {
                cloudinaryClient.deleteImage(oldPublicId);
                log.info("Deleted old variant image from Cloudinary: {}", oldPublicId);
            } catch (Exception e) {
                log.warn("Failed to delete old variant image from Cloudinary: {}", oldPublicId, e);
                // Non-critical error, continue
            }
        }

        log.info("Successfully uploaded variant image for '{}' on product {}", attributeValue, productId);

        return VariantImageResponse.from(productImage);
    }

    @Override
    @Transactional
    public void deleteVariantImage(Long productId, String attributeValue, Long userId) {
        log.info("Deleting variant image for product {} with attribute value '{}' by user {}", 
                 productId, attributeValue, userId);

        // Verify ownership
        findProductAndVerifyOwnership(productId, userId);

        // Verify product has primary attribute
        productPrimaryAttributeRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(
                    "Product does not have a primary attribute."));

        // Find variant image (use simpler query to avoid duplicates)
        ProductImage image = productImageRepository
                .findVariantImageByProductIdAndAttributeValue(productId, attributeValue)
                .orElseThrow(() -> new ImageNotFoundException(
                    String.format("Variant image not found for attribute value '%s'", attributeValue)));

        String publicId = image.getPublicId();

        // Delete from Cloudinary
        try {
            boolean deleted = cloudinaryClient.deleteImage(publicId);
            if (!deleted) {
                log.warn("Cloudinary reported variant image not deleted: {}", publicId);
            }
        } catch (Exception e) {
            log.error("Failed to delete variant image from Cloudinary: {}", e.getMessage(), e);
            throw new CloudinaryServiceException("Failed to delete variant image from Cloudinary: " + e.getMessage(), e);
        }

        // Delete from database
        productImageRepository.delete(image);

        log.info("Successfully deleted variant image for '{}' on product {}: {}", attributeValue, productId, publicId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductImagesResponse getProductImages(Long productId) {
        log.debug("Retrieving all images for product {}", productId);

        // Get product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new com.PBL6.Ecommerce.exception.ProductNotFoundException(
                    "Product not found with id: " + productId));

        // Get main image
        String mainImage = product.getMainImage();

        // Get gallery images
        List<ProductImage> galleryImageEntities = productImageRepository.findGalleryImagesByProductId(productId);
        List<GalleryImageResponse> galleryImages = galleryImageEntities.stream()
                .map((ProductImage img) -> GalleryImageResponse.builder()
                        .id(img.getId())
                        .url(img.getImageUrl())
                        .publicId(img.getPublicId())
                        .displayOrder(img.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());

        // Get primary attribute
        ProductPrimaryAttribute primaryAttr = productPrimaryAttributeRepository.findByProductId(productId)
                .orElse(null);

        PrimaryAttributeDTO primaryAttributeDTO = null;
        Map<String, VariantImageResponse> variantImagesMap = new HashMap<>();

        if (primaryAttr != null) {
            Long attributeId = primaryAttr.getAttribute().getId();
            String attributeName = primaryAttr.getAttribute().getName();

            // Get distinct values for primary attribute
            List<String> values = productVariantValueRepository
                    .findDistinctValuesByProductIdAndAttributeId(productId, attributeId);

            primaryAttributeDTO = PrimaryAttributeDTO.builder()
                    .id(attributeId)
                    .name(attributeName)
                    .values(values)
                    .build();

            // Get variant images
            List<ProductImage> variantImageEntities = productImageRepository
                    .findByProductIdAndImageType(productId, "VARIANT");

            variantImagesMap = variantImageEntities.stream()
                    .collect(Collectors.toMap(
                        ProductImage::getVariantAttributeValue,
                        VariantImageResponse::from,
                        (existing, replacement) -> replacement // Handle duplicates
                    ));
        }

        return ProductImagesResponse.builder()
                .mainImage(mainImage)
                .galleryImages(galleryImages)
                .primaryAttribute(primaryAttributeDTO)
                .variantImages(variantImagesMap)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getPrimaryAttributeValues(Long productId) {
        log.debug("Getting primary attribute values for product {}", productId);

        // Verify product exists
        productRepository.findById(productId)
                .orElseThrow(() -> new com.PBL6.Ecommerce.exception.ProductNotFoundException(
                    "Product not found with id: " + productId));

        // Get primary attribute values using the improved query
        List<String> values = productVariantValueRepository.findPrimaryAttributeValuesByProductId(productId);
        
        log.debug("Found {} primary attribute values for product {}: {}", values.size(), productId, values);
        return values;
    }
}
