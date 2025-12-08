package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.SearchSynonym;
import com.PBL6.Ecommerce.domain.SearchSynonym.SynonymType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchSynonymRepository extends JpaRepository<SearchSynonym, Long> {
    
    /**
     * Find synonym for a term (for typo correction)
     */
    @Query("SELECT ss FROM SearchSynonym ss WHERE ss.term = :term AND ss.isActive = true ORDER BY ss.priority DESC")
    List<SearchSynonym> findByTerm(@Param("term") String term);
    
    /**
     * Find highest priority synonym for a term
     */
    @Query("SELECT ss FROM SearchSynonym ss WHERE ss.term = :term AND ss.isActive = true ORDER BY ss.priority DESC LIMIT 1")
    Optional<SearchSynonym> findBestMatch(@Param("term") String term);
    
    /**
     * Find synonyms by type
     */
    List<SearchSynonym> findByTypeAndIsActiveTrue(SynonymType type);
    
    /**
     * Check if term has a correction
     */
    boolean existsByTermAndIsActiveTrue(String term);
    
    /**
     * Find all corrections for typo detection
     */
    @Query("SELECT ss FROM SearchSynonym ss WHERE ss.type = 'TYPO' AND ss.isActive = true")
    List<SearchSynonym> findAllTypoCorrections();
    
    /**
     * Find term containing partial match (for fuzzy matching)
     */
    @Query("SELECT ss FROM SearchSynonym ss WHERE ss.term LIKE LOWER(CONCAT('%', :partial, '%')) AND ss.isActive = true ORDER BY ss.priority DESC")
    List<SearchSynonym> findByPartialTerm(@Param("partial") String partial);
}
