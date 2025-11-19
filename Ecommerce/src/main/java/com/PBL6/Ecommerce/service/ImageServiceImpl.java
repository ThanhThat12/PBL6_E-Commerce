package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.constant.ImageType;
import com.PBL6.Ecommerce.constant.TransformationType;
import com.PBL6.Ecommerce.domain.Product;
import com.PBL6.Ecommerce.domain.ProductImage;
import com.PBL6.Ecommerce.domain.ProductReview;
import com.PBL6.Ecommerce.domain.ProductVariant;
import com.PBL6.Ecommerce.domain.ProductVariantValue;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.dto.cloudinary.CloudinaryUploadResult;
import com.PBL6.Ecommerce.dto.request.ImageReorderRequest;
import com.PBL6.Ecommerce.dto.response.ImageDeleteResponse;
import com.PBL6.Ecommerce.dto.response.ImageTransformationResponse;
import com.PBL6.Ecommerce.dto.response.ImageUploadResponse;
import com.PBL6.Ecommerce.dto.response.ProductImageResponse;
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

        // Update product entity
        product.setMainImage(result.getSecureUrl());
        product.setMainImagePublicId(result.getPublicId());
        productRepository.save(product);

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
        ProductVariant variant = null;
        if (variantId != null) {
            variant = productVariantRepository.findById(variantId)
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
                productImage.setVariant(variant); // Associate with variant if provided
                productImage.setImageUrl(result.getSecureUrl());
                productImage.setPublicId(result.getPublicId());
                productImage.setDisplayOrder(nextDisplayOrder++);
                // productImage.setCreatedAt(LocalDateTime.now());
                // productImage.setUpdatedAt(LocalDateTime.now());

                productImageRepository.save(productImage);
                
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
    public List<ProductImageResponse> getProductGalleryImages(Long productId, Long variantId) {
        log.debug("Retrieving gallery images for product {} (variant: {})", productId, variantId);

        List<ProductImage> images;
        if (variantId != null) {
            images = productImageRepository.findByProductIdAndVariantIdOrderByDisplayOrderAsc(productId, variantId);
        } else {
            images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId);
        }

        return images.stream()
                .map(this::mapToProductImageResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void reorderProductGalleryImages(Long productId, ImageReorderRequest request, Long userId) {
        log.info("Reordering {} gallery images for product {} by user {}", 
                 request.getImageIds().size(), productId, userId);

        // Verify ownership
        findProductAndVerifyOwnership(productId, userId);

        // Validate and fetch images
        List<Long> imageIds = request.getImageIds();
        if (imageIds == null || imageIds.isEmpty()) {
            throw new ImageValidationException("Image IDs list cannot be empty");
        }

        // Update display order
        for (int i = 0; i < imageIds.size(); i++) {
            Long imageId = imageIds.get(i);
            ProductImage image = productImageRepository.findById(imageId)
                    .orElseThrow(() -> new ImageNotFoundException("Image not found: " + imageId));

            // Verify image belongs to the product
            if (!image.getProduct().getId().equals(productId)) {
                throw new ImageValidationException("Image " + imageId + " does not belong to product " + productId);
            }

            image.setDisplayOrder(i);
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

            // Save to database with both variant and variantValue associations
            ProductImage productImage = new ProductImage();
            productImage.setProduct(product);
            productImage.setImageUrl(result.getUrl());
            productImage.setPublicId(result.getPublicId());
            productImage.setVariantValueName(variantValueName); // Display-friendly name
            productImage.setDisplayOrder(0); // Single image per variant value
            
            // NEW: Set both associations for optimal queries
            productImage.setVariant(variant);  // Specific variant (for variant_id queries)
            productImage.setVariantValue(variantValue);  // Group 1 value (for optimized queries)
            
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
                .variantId(image.getVariant() != null ? image.getVariant().getId() : null)
                .variantValueName(image.getVariantValueName())
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
}
