package com.PBL6.Ecommerce.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for tracking search queries
 * Used for analytics, trending, and personalized suggestions
 */
@Entity
@Table(name = "search_queries", indexes = {
    @Index(name = "idx_search_query", columnList = "query"),
    @Index(name = "idx_search_user", columnList = "user_id"),
    @Index(name = "idx_search_created_at", columnList = "created_at"),
    @Index(name = "idx_search_normalized", columnList = "normalized_query")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchQuery {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;
    
    @Column(nullable = false, length = 255)
    private String query;
    
    @Column(name = "normalized_query", length = 255)
    private String normalizedQuery;
    
    @Column(name = "result_count")
    private Integer resultCount = 0;
    
    @Column(name = "filters_applied", columnDefinition = "JSON")
    private String filtersApplied;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clicked_product_id", nullable = true)
    private Product clickedProduct;
    
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (query != null) {
            normalizedQuery = query.toLowerCase().trim();
        }
    }
}
