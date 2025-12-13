package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.entity.search.TrendingSearch;
import com.PBL6.Ecommerce.domain.entity.search.TrendingSearch.TrendPeriod;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrendingSearchRepository extends JpaRepository<TrendingSearch, Long> {
    
    /**
     * Get top trending searches by period
     */
    @Query("SELECT ts FROM TrendingSearch ts WHERE ts.period = :period ORDER BY ts.trendScore DESC")
    List<TrendingSearch> findTopByPeriod(@Param("period") TrendPeriod period, Pageable pageable);
    
    /**
     * Find by normalized query and period
     */
    Optional<TrendingSearch> findByNormalizedQueryAndPeriod(String normalizedQuery, TrendPeriod period);
    
    /**
     * Increment search count for existing trending search
     */
    @Modifying
    @Transactional
    @Query("UPDATE TrendingSearch ts SET ts.searchCount = ts.searchCount + 1, ts.lastSearched = CURRENT_TIMESTAMP WHERE ts.id = :id")
    void incrementSearchCount(@Param("id") Long id);
    
    /**
     * Increment click count when user clicks a search result
     */
    @Modifying
    @Transactional
    @Query("UPDATE TrendingSearch ts SET ts.clickCount = ts.clickCount + 1 WHERE ts.normalizedQuery = :query AND ts.period = :period")
    void incrementClickCount(@Param("query") String normalizedQuery, @Param("period") TrendPeriod period);
    
    /**
     * Update trend scores (called by scheduled job)
     * Score = (searchCount * 0.6) + (clickCount * 0.4) * recency_factor
     */
    @Modifying
    @Transactional
    @Query("UPDATE TrendingSearch ts SET ts.trendScore = " +
           "(ts.searchCount * 0.6 + ts.clickCount * 0.4) * " +
           "(1.0 / (1 + DATEDIFF(CURRENT_DATE, ts.lastSearched)))")
    void updateTrendScores();
    
    /**
     * Get trending searches matching a prefix (for autocomplete)
     */
    @Query("SELECT ts FROM TrendingSearch ts WHERE ts.period = :period " +
           "AND ts.normalizedQuery LIKE LOWER(CONCAT(:prefix, '%')) " +
           "ORDER BY ts.trendScore DESC")
    List<TrendingSearch> findByPrefixAndPeriod(@Param("prefix") String prefix, 
                                                @Param("period") TrendPeriod period, 
                                                Pageable pageable);
}
