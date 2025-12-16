package com.PBL6.Ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.domain.entity.product.Product;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.repository.ProductReviewRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Scheduled job service to sync product ratings from reviews
 * Runs daily at 2 AM to update product rating and review count
 */
@Service
@RequiredArgsConstructor
public class ProductRatingScheduler {

    private static final Logger log = LoggerFactory.getLogger(ProductRatingScheduler.class);
    
    private final ProductRepository productRepository;
    private final ProductReviewRepository productReviewRepository;

    /**
     * Sync all product ratings from reviews
     * Cron: Run daily at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void syncAllProductRatings() {
        log.info("🔄 Starting scheduled product rating sync...");
        
        try {
            List<Product> allProducts = productRepository.findAll();
            int updatedCount = 0;
            int skippedCount = 0;
            
            for (Product product : allProducts) {
                try {
                    // Get average rating from reviews
                    Double averageRating = productReviewRepository.getAverageRatingByProductId(product.getId());
                    long reviewCount = productReviewRepository.countByProductId(product.getId());
                    
                    // Check if update is needed
                    BigDecimal currentRating = product.getRating();
                    int currentReviewCount = product.getReviewCount() != null ? product.getReviewCount() : 0;
                    
                    BigDecimal newRating = (averageRating != null && averageRating > 0) 
                        ? BigDecimal.valueOf(averageRating).setScale(2, java.math.RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                    
                    // Only update if values changed
                    if (!newRating.equals(currentRating) || currentReviewCount != reviewCount) {
                        product.setRating(newRating);
                        product.setReviewCount((int) reviewCount);
                        product.setUpdatedAt(java.time.LocalDateTime.now());
                        productRepository.save(product);
                        updatedCount++;
                        
                        log.debug("Updated product {} rating: {} → {} ({} reviews)", 
                            product.getId(), currentRating, newRating, reviewCount);
                    } else {
                        skippedCount++;
                    }
                    
                } catch (Exception e) {
                    log.error("Error updating rating for product {}: {}", product.getId(), e.getMessage());
                }
            }
            
            log.info("✅ Product rating sync completed: {} updated, {} skipped, {} total", 
                updatedCount, skippedCount, allProducts.size());
            
        } catch (Exception e) {
            log.error("❌ Error during product rating sync: {}", e.getMessage(), e);
        }
    }

    /**
     * Manual sync trigger for testing
     * Can be called from admin API endpoint
     */
    @Transactional
    public String syncAllProductRatingsManual() {
        log.info("🔄 Manual product rating sync triggered...");
        syncAllProductRatings();
        return "Product rating sync completed";
    }
}
