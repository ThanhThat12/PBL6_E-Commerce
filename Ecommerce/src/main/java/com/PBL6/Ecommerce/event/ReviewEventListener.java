package com.PBL6.Ecommerce.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.PBL6.Ecommerce.service.ProductService;

/**
 * Async event listener for review changes.
 * Handles rating recalculation after review transactions commit successfully.
 * 
 * Benefits:
 * - Zero lock contention (runs after commit in separate thread)
 * - Fast API response (review operations return immediately)
 * - Automatic retry via ProductService retry logic
 * - Failure isolation (rating update failures don't break review operations)
 */
@Component
public class ReviewEventListener {
    
    private static final Logger log = LoggerFactory.getLogger(ReviewEventListener.class);
    
    @Autowired
    private ProductService productService;
    
    /**
     * Handle review change events asynchronously after transaction commit.
     * Uses @Async to run in separate thread pool.
     * Uses AFTER_COMMIT to ensure review is persisted before rating update.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewChanged(ReviewChangedEvent event) {
        try {
            log.info("Processing async rating update for product {} after review {}", 
                     event.getProductId(), event.getOperation());
            
            // Call existing retry-enabled rating update method
            productService.updateProductRating(event.getProductId());
            
            log.info("Async rating update completed for product {}", event.getProductId());
        } catch (Exception e) {
            // Log but don't throw - rating update is best-effort
            log.error("Async rating update failed for product {} after review {}: {}", 
                      event.getProductId(), event.getOperation(), e.getMessage());
        }
    }
}
