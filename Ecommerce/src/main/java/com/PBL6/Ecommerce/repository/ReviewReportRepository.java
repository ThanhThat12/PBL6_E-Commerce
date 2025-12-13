package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.entity.product.ReviewReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
    
    /**
     * Check if user already reported a review
     */
    boolean existsByReportedByIdAndReviewId(Long reportedById, Long reviewId);
    
    /**
     * Find reports by status
     */
    Page<ReviewReport> findByStatus(ReviewReport.ReportStatus status, Pageable pageable);
    
    /**
     * Count pending reports
     */
    Long countByStatus(ReviewReport.ReportStatus status);
    
    /**
     * Delete all reports for a review (when review is deleted)
     */
    void deleteByReviewId(Long reviewId);
}
