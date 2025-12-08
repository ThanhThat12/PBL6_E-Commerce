package com.PBL6.Ecommerce.domain.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for search suggestions response
 * Contains query suggestions, product suggestions, and category suggestions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchSuggestionDTO {
    
    /**
     * Query/keyword suggestions (autocomplete)
     */
    private List<QuerySuggestionItem> queries;
    
    /**
     * Direct product matches (mini cards)
     */
    private List<ProductSuggestionItem> products;
    
    /**
     * Category matches
     */
    private List<CategorySuggestionItem> categories;
    
    /**
     * Trending/popular searches
     */
    private List<TrendingSuggestionItem> trending;
    
    /**
     * Shop matches
     */
    private List<ShopSuggestionItem> shops;
    
    /**
     * Typo correction suggestion
     */
    private String didYouMean;
    
    /**
     * Query suggestion item
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuerySuggestionItem {
        private String text;
        private String highlightedText; // With <b> tags for matching parts
        private Long estimatedCount;
    }
    
    /**
     * Product suggestion item (mini card)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSuggestionItem {
        private Long id;
        private String name;
        private String highlightedName;
        private String image;
        private BigDecimal price;
        private BigDecimal rating;
        private Integer soldCount;
        private String shopName;
    }
    
    /**
     * Category suggestion item
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySuggestionItem {
        private Long id;
        private String name;
        private String highlightedName;
        private Long productCount;
    }
    
    /**
     * Trending search item
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendingSuggestionItem {
        private String query;
        private Integer rank;
        private Long searchCount;
    }
    
    /**
     * Shop suggestion item
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopSuggestionItem {
        private Long id;
        private String name;
        private String highlightedName;
        private String logoUrl;
        private Long productCount;
        private String status;
    }
}
