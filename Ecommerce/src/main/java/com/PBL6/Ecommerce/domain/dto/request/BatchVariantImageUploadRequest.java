package com.PBL6.Ecommerce.domain.dto.request;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for batch variant image upload
 * Supports uploading images for multiple Group 1 variants in a single API call
 * Each Group 1 variant value MUST have exactly 1 image.
 * 
 * Format: Multipart form-data with:
 * - files[]: Array of image files
 * - variantMappings: JSON mapping Group 1 value names to file indices
 * 
 * Example variantMappings: {"Red": [0], "Blue": [1], "Green": [2]}
 * - Key: Group 1 variant value name (e.g., "Red", "Blue", "Small", "Cotton")
 * - Value: Array with exactly 1 index referencing files[] array
 */
public class BatchVariantImageUploadRequest {

    /**
     * Mapping from Group 1 variant value name to file index (exactly 1 per value)
     * Example: {"Red": [0], "Blue": [1], "Green": [2]}
     * Backend will resolve variant names to variant_value_ids
     */
    @NotNull(message = "Variant mappings are required")
    private Map<String, List<Integer>> variantMappings;

    /**
     * Product ID for which images are being uploaded
     */
    @NotNull(message = "Product ID is required")
    private Long productId;

    // Constructors
    public BatchVariantImageUploadRequest() {}

    public BatchVariantImageUploadRequest(Map<String, List<Integer>> variantMappings, Long productId) {
        this.variantMappings = variantMappings;
        this.productId = productId;
    }

    // Getters and Setters
    public Map<String, List<Integer>> getVariantMappings() {
        return variantMappings;
    }

    public void setVariantMappings(Map<String, List<Integer>> variantMappings) {
        this.variantMappings = variantMappings;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    /**
     * Get total number of files expected
     */
    public int getTotalFileCount() {
        if (variantMappings == null) {
            return 0;
        }
        return variantMappings.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    /**
     * Validate that each variant value has exactly 1 image and all indices are unique and within bounds
     */
    public boolean isValid(int actualFileCount) {
        if (variantMappings == null || variantMappings.isEmpty()) {
            return false;
        }

        // NEW: Enforce exactly 1 image per variant value
        for (Map.Entry<String, List<Integer>> entry : variantMappings.entrySet()) {
            List<Integer> indices = entry.getValue();
            if (indices == null || indices.size() != 1) {
                return false; // Each variant value must have exactly 1 image
            }
        }

        // Check for duplicate indices
        long distinctIndices = variantMappings.values().stream()
                .flatMap(List::stream)
                .distinct()
                .count();
        
        long totalIndices = variantMappings.values().stream()
                .mapToInt(List::size)
                .sum();

        if (distinctIndices != totalIndices) {
            return false; // Duplicate indices found
        }

        // Check all indices are within bounds
        int maxIndex = variantMappings.values().stream()
                .flatMap(List::stream)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(-1);

        return maxIndex < actualFileCount && maxIndex >= 0;
    }

    @Override
    public String toString() {
        return "BatchVariantImageUploadRequest{" +
                "productId=" + productId +
                ", variantMappings=" + variantMappings +
                ", totalFiles=" + getTotalFileCount() +
                '}';
    }
}
