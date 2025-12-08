package com.PBL6.Ecommerce.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for trending/popular searches
 * Aggregated from search_queries for fast lookup
 */
@Entity
@Table(name = "trending_searches", indexes = {
    @Index(name = "idx_trend_score", columnList = "trend_score DESC"),
    @Index(name = "idx_period_score", columnList = "period, trend_score DESC"),
    @Index(name = "idx_last_searched", columnList = "last_searched")
}, uniqueConstraints = {
    @UniqueConstraint(name = "unique_normalized_query_period", columnNames = {"normalized_query", "period"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendingSearch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String query;
    
    @Column(name = "normalized_query", nullable = false, length = 255)
    private String normalizedQuery;
    
    @Column(name = "search_count")
    private Integer searchCount = 1;
    
    @Column(name = "click_count")
    private Integer clickCount = 0;
    
    @Column(name = "last_searched")
    private LocalDateTime lastSearched;
    
    @Column(name = "trend_score", precision = 10, scale = 2)
    private BigDecimal trendScore = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private TrendPeriod period = TrendPeriod.DAILY;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    public enum TrendPeriod {
        HOURLY, DAILY, WEEKLY, MONTHLY
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastSearched = LocalDateTime.now();
        if (query != null) {
            normalizedQuery = query.toLowerCase().trim();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastSearched = LocalDateTime.now();
    }
}
