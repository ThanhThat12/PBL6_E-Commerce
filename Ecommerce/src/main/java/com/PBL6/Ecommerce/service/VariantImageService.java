package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.ProductVariant;
import com.PBL6.Ecommerce.domain.VariantImage;
import com.PBL6.Ecommerce.dto.seller.VariantImageDTO;
import com.PBL6.Ecommerce.repository.ProductVariantRepository;
import com.PBL6.Ecommerce.repository.VariantImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing variant-specific images
 * Handles uploading, retrieving, updating and deleting images per product variant
 */
@Service
public class VariantImageService {

    @Autowired
    private VariantImageRepository variantImageRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    /**
     * Upload an image for a product variant
     * @param variantId The variant to upload image for
     * @param file The image file
     * @param displayOrder The display order in gallery
     * @param altText Alternative text for image
     * @return VariantImageDTO of uploaded image
     */
    public VariantImageDTO uploadVariantImage(Long variantId, MultipartFile file, Integer displayOrder, String altText) {
        // Get variant
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found with id: " + variantId));

        // Upload to Cloudinary
        String imageUrl = cloudinaryService.uploadProductImage(file, variant.getProduct().getId());

        // Create and save VariantImage
        VariantImage image = new VariantImage();
        image.setVariant(variant);
        image.setImageUrl(imageUrl);
        image.setDisplayOrder(displayOrder != null ? displayOrder : 0);
        image.setAltText(altText);
        image.setIsActive(true);

        VariantImage savedImage = variantImageRepository.save(image);
        return convertToDTO(savedImage);
    }

    /**
     * Get all images for a variant
     * @param variantId The variant ID
     * @return List of VariantImageDTOs
     */
    public List<VariantImageDTO> getVariantImages(Long variantId) {
        List<VariantImage> images = variantImageRepository.findByVariantIdOrderByDisplayOrder(variantId);
        return images.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get only active images for a variant
     * @param variantId The variant ID
     * @return List of active VariantImageDTOs
     */
    public List<VariantImageDTO> getActiveVariantImages(Long variantId) {
        List<VariantImage> images = variantImageRepository.findByVariantIdAndIsActiveTrue(variantId);
        return images.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get the main image (displayOrder = 0) for a variant
     * @param variantId The variant ID
     * @return VariantImageDTO of main image, or null if not found
     */
    public VariantImageDTO getMainImage(Long variantId) {
        return variantImageRepository.findByVariantIdAndDisplayOrder(variantId, 0)
                .map(this::convertToDTO)
                .orElse(null);
    }

    /**
     * Update variant image metadata
     * @param variantId The variant ID
     * @param imageId The image ID
     * @param newDisplayOrder New display order
     * @param newAltText New alt text
     * @return Updated VariantImageDTO
     */
    public VariantImageDTO updateVariantImage(Long variantId, Long imageId, Integer newDisplayOrder, String newAltText) {
        VariantImage image = variantImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found with id: " + imageId));

        // Verify image belongs to variant
        if (image.getVariant() == null || !image.getVariant().getId().equals(variantId)) {
            throw new IllegalArgumentException("Image does not belong to variant: " + variantId);
        }

        if (newDisplayOrder != null) {
            image.setDisplayOrder(newDisplayOrder);
        }
        if (newAltText != null) {
            image.setAltText(newAltText);
        }

        VariantImage updated = variantImageRepository.save(image);
        return convertToDTO(updated);
    }

    /**
     * Delete a variant image
     * @param variantId The variant ID
     * @param imageId The image ID
     */
    public void deleteVariantImage(Long variantId, Long imageId) {
        VariantImage image = variantImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found with id: " + imageId));

        // Verify image belongs to variant
        if (image.getVariant() == null || !image.getVariant().getId().equals(variantId)) {
            throw new IllegalArgumentException("Image does not belong to variant: " + variantId);
        }

        // Try to delete from Cloudinary
        try {
            String publicId = cloudinaryService.extractPublicId(image.getImageUrl());
            if (publicId != null) {
                cloudinaryService.deleteImage(publicId);
            }
        } catch (Exception e) {
            // Log error but continue with database deletion
            System.err.println("Failed to delete image from Cloudinary: " + e.getMessage());
        }

        // Delete from database
        variantImageRepository.delete(image);
    }

    /**
     * Reorder images for a variant
     * @param variantId The variant ID
     * @param imageIds List of image IDs in desired order
     * @return List of reordered VariantImageDTOs
     */
    public List<VariantImageDTO> reorderVariantImages(Long variantId, List<Long> imageIds) {
        List<VariantImage> images = variantImageRepository.findByVariantIdOrderByDisplayOrder(variantId);
        
        for (int i = 0; i < imageIds.size(); i++) {
            Long imageId = imageIds.get(i);
            VariantImage image = images.stream()
                    .filter(img -> img.getId().equals(imageId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));
            
            image.setDisplayOrder(i);
            variantImageRepository.save(image);
        }

        // Get and return updated list
        return getVariantImages(variantId);
    }

    /**
     * Delete all images for a variant
     * @param variantId The variant ID
     */
    public void deleteAllVariantImages(Long variantId) {
        List<VariantImage> images = variantImageRepository.findByVariantIdOrderByDisplayOrder(variantId);
        
        for (VariantImage image : images) {
            try {
                String publicId = cloudinaryService.extractPublicId(image.getImageUrl());
                if (publicId != null) {
                    cloudinaryService.deleteImage(publicId);
                }
            } catch (Exception e) {
                System.err.println("Failed to delete image from Cloudinary: " + e.getMessage());
            }
        }

        variantImageRepository.deleteByVariantId(variantId);
    }

    /**
     * Convert VariantImage entity to DTO
     */
    private VariantImageDTO convertToDTO(VariantImage image) {
        VariantImageDTO dto = new VariantImageDTO();
        dto.setId(image.getId());
        dto.setVariantId(image.getVariant() != null ? image.getVariant().getId() : null);
        dto.setImageUrl(image.getImageUrl());
        dto.setDisplayOrder(image.getDisplayOrder());
        dto.setAltText(image.getAltText());
        dto.setIsActive(image.getIsActive());
        dto.setCreatedAt(image.getCreatedAt());
        return dto;
    }
}
