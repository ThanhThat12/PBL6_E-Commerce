package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.SearchQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SearchQueryRepository extends JpaRepository<SearchQuery, Long> {
    
    /**
     * Get recent searches by user
     */
    @Query("SELECT sq FROM SearchQuery sq WHERE sq.user.id = :userId ORDER BY sq.createdAt DESC")
    Page<SearchQuery> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Get distinct recent queries by user (for history display)
     */
    @Query("SELECT sq.query FROM SearchQuery sq WHERE sq.user.id = :userId GROUP BY sq.query ORDER BY MAX(sq.createdAt) DESC")
    List<String> findDistinctRecentQueriesByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Count searches for a normalized query in time period
     */
    @Query("SELECT COUNT(sq) FROM SearchQuery sq WHERE sq.normalizedQuery = :query AND sq.createdAt >= :since")
    Long countByNormalizedQuerySince(@Param("query") String normalizedQuery, @Param("since") LocalDateTime since);
    
    /**
     * Get popular queries in time period (for trending calculation)
     */
    @Query("SELECT sq.normalizedQuery, COUNT(sq) as cnt FROM SearchQuery sq " +
           "WHERE sq.createdAt >= :since " +
           "GROUP BY sq.normalizedQuery " +
           "ORDER BY cnt DESC")
    List<Object[]> findPopularQueriesSince(@Param("since") LocalDateTime since, Pageable pageable);
    
    /**
     * Delete old search queries (for cleanup)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM SearchQuery sq WHERE sq.createdAt < :before")
    void deleteOlderThan(@Param("before") LocalDateTime before);
    
    /**
     * Delete all search history for a user
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM SearchQuery sq WHERE sq.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
    
    /**
     * Delete specific query from user's history
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM SearchQuery sq WHERE sq.user.id = :userId AND sq.normalizedQuery = :normalizedQuery")
    void deleteByUserIdAndNormalizedQuery(@Param("userId") Long userId, @Param("normalizedQuery") String normalizedQuery);
}
