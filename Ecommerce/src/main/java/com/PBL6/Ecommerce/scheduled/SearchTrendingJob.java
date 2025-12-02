package com.PBL6.Ecommerce.scheduled;

import com.PBL6.Ecommerce.domain.TrendingSearch;
import com.PBL6.Ecommerce.domain.TrendingSearch.TrendPeriod;
import com.PBL6.Ecommerce.repository.SearchQueryRepository;
import com.PBL6.Ecommerce.repository.TrendingSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Scheduled job to calculate and update trending searches
 * Runs periodically to aggregate search data and update trend scores
 */
@Component
public class SearchTrendingJob {
    
    private static final Logger log = LoggerFactory.getLogger(SearchTrendingJob.class);
    
    @Autowired
    private SearchQueryRepository searchQueryRepository;
    
    @Autowired
    private TrendingSearchRepository trendingSearchRepository;
    
    /**
     * Update daily trending searches - runs every hour
     * Aggregates searches from the last 24 hours
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    @Transactional
    public void updateDailyTrending() {
        log.info("Starting daily trending calculation job...");
        
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(1);
            List<Object[]> popularQueries = searchQueryRepository.findPopularQueriesSince(
                since, PageRequest.of(0, 100));
            
            int updatedCount = 0;
            for (Object[] row : popularQueries) {
                String normalizedQuery = (String) row[0];
                Long count = (Long) row[1];
                
                updateOrCreateTrending(normalizedQuery, count.intValue(), TrendPeriod.DAILY);
                updatedCount++;
            }
            
            // Update trend scores
            trendingSearchRepository.updateTrendScores();
            
            log.info("Daily trending calculation completed. Updated {} entries.", updatedCount);
        } catch (Exception e) {
            log.error("Error in daily trending calculation", e);
        }
    }
    
    /**
     * Update weekly trending searches - runs every 6 hours
     */
    @Scheduled(fixedRate = 21600000) // Every 6 hours
    @Transactional
    public void updateWeeklyTrending() {
        log.info("Starting weekly trending calculation job...");
        
        try {
            LocalDateTime since = LocalDateTime.now().minusWeeks(1);
            List<Object[]> popularQueries = searchQueryRepository.findPopularQueriesSince(
                since, PageRequest.of(0, 50));
            
            int updatedCount = 0;
            for (Object[] row : popularQueries) {
                String normalizedQuery = (String) row[0];
                Long count = (Long) row[1];
                
                updateOrCreateTrending(normalizedQuery, count.intValue(), TrendPeriod.WEEKLY);
                updatedCount++;
            }
            
            log.info("Weekly trending calculation completed. Updated {} entries.", updatedCount);
        } catch (Exception e) {
            log.error("Error in weekly trending calculation", e);
        }
    }
    
    /**
     * Cleanup old search queries - runs daily at 3 AM
     * Keeps only last 30 days of search history
     */
    @Scheduled(cron = "0 0 3 * * ?") // 3 AM daily
    @Transactional
    public void cleanupOldSearchQueries() {
        log.info("Starting search query cleanup job...");
        
        try {
            LocalDateTime before = LocalDateTime.now().minusDays(30);
            searchQueryRepository.deleteOlderThan(before);
            log.info("Cleanup completed. Deleted queries older than {}", before);
        } catch (Exception e) {
            log.error("Error in search query cleanup", e);
        }
    }
    
    /**
     * Helper to update or create trending search entry
     */
    private void updateOrCreateTrending(String normalizedQuery, int count, TrendPeriod period) {
        Optional<TrendingSearch> existing = trendingSearchRepository
            .findByNormalizedQueryAndPeriod(normalizedQuery, period);
        
        if (existing.isPresent()) {
            TrendingSearch trending = existing.get();
            trending.setSearchCount(count);
            // Calculate trend score: count * recency factor
            BigDecimal score = BigDecimal.valueOf(count);
            trending.setTrendScore(score);
            trendingSearchRepository.save(trending);
        } else {
            TrendingSearch trending = new TrendingSearch();
            trending.setQuery(normalizedQuery);
            trending.setNormalizedQuery(normalizedQuery);
            trending.setSearchCount(count);
            trending.setPeriod(period);
            trending.setTrendScore(BigDecimal.valueOf(count));
            trendingSearchRepository.save(trending);
        }
    }
}
