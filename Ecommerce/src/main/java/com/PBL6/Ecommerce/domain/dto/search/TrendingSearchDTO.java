package com.PBL6.Ecommerce.domain.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for trending searches response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendingSearchDTO {
    private List<TrendingItem> trending;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendingItem {
        private String query;
        private Integer rank;
        private Long searchCount;
        private Double trendScore;
    }
}
