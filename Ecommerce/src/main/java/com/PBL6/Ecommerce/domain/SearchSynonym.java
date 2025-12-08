package com.PBL6.Ecommerce.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for search synonyms and typo corrections
 * Used for query expansion and spell correction
 */
@Entity
@Table(name = "search_synonyms", indexes = {
    @Index(name = "idx_term", columnList = "term"),
    @Index(name = "idx_type", columnList = "type"),
    @Index(name = "idx_active", columnList = "is_active")
}, uniqueConstraints = {
    @UniqueConstraint(name = "unique_term_synonym", columnNames = {"term", "synonym"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchSynonym {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String term;
    
    @Column(nullable = false, length = 100)
    private String synonym;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SynonymType type = SynonymType.SYNONYM;
    
    @Column
    private Integer priority = 0;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    public enum SynonymType {
        TYPO,       // Spelling correction
        SYNONYM,    // Same meaning
        ALIAS,      // Abbreviation/nickname
        BRAND       // Brand name variations
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
