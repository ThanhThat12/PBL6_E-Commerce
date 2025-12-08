package com.PBL6.Ecommerce.domain.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for faceted search filters with counts
 * Provides filter options with the number of products matching each filter
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacetedSearchDTO {
    
    /**
     * Total number of products matching the search
     */
    private long totalCount;
    
    /**
     * Category facets with product counts
     */
    private List<CategoryFacet> categories;
    
    /**
     * Price range facets with product counts
     */
    private List<PriceRangeFacet> priceRanges;
    
    /**
     * Rating facets with product counts
     */
    private List<RatingFacet> ratings;
    
    /**
     * Brand/Shop facets with product counts
     */
    private List<BrandFacet> brands;
    
    // ========== NESTED CLASSES ==========
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryFacet {
        private Long id;
        private String name;
        private Long productCount;
        private boolean selected;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceRangeFacet {
        private String label;
        private Double minPrice;
        private Double maxPrice;
        private Long productCount;
        private boolean selected;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingFacet {
        private int minRating;
        private String label;
        private Long productCount;
        private boolean selected;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandFacet {
        private Long shopId;
        private String shopName;
        private Long productCount;
        private boolean selected;
    }
}
