package com.PBL6.Ecommerce.scheduler;

import com.PBL6.Ecommerce.domain.entity.order.Order;
import com.PBL6.Ecommerce.domain.entity.product.Product;
import com.PBL6.Ecommerce.repository.OrderItemRepository;
import com.PBL6.Ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Scheduled job to sync product sold count from completed orders
 * Runs daily at 3:00 AM
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSoldCountScheduler {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * Sync all products' soldCount from completed orders
     * Daily at 3:00 AM
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void syncAllProductsSoldCount() {
        log.info("========== Starting Product Sold Count Sync ==========");
        
        try {
            List<Product> allProducts = productRepository.findAll();
            log.info("Found {} products to sync sold count", allProducts.size());
            
            int updatedCount = 0;
            int skippedCount = 0;
            
            for (Product product : allProducts) {
                try {
                    // Calculate total sold from COMPLETED orders
                    Long totalSold = orderItemRepository.calculateTotalSoldByProductId(product.getId());
                    int newSoldCount = (totalSold != null) ? totalSold.intValue() : 0;
                    
                    // Update only if changed
                    Integer currentSoldCount = product.getSoldCount() != null ? product.getSoldCount() : 0;
                    if (!currentSoldCount.equals(newSoldCount)) {
                        int oldSoldCount = currentSoldCount;
                        product.setSoldCount(newSoldCount);
                        productRepository.save(product);
                        updatedCount++;
                        
                        log.debug("Updated product ID {} soldCount: {} -> {}", 
                                 product.getId(), oldSoldCount, newSoldCount);
                    } else {
                        skippedCount++;
                    }
                    
                } catch (Exception e) {
                    log.error("Error syncing soldCount for product ID {}: {}", 
                             product.getId(), e.getMessage());
                }
            }
            
            log.info("========== Product Sold Count Sync Completed ==========");
            log.info("Total products: {}, Updated: {}, Skipped: {}", 
                     allProducts.size(), updatedCount, skippedCount);
            
        } catch (Exception e) {
            log.error("Fatal error during product sold count sync: {}", e.getMessage(), e);
        }
    }

    /**
     * Manual trigger for testing (can be called via API endpoint)
     */
    @Transactional
    public void syncProductSoldCount(Long productId) {
        log.info("Manual sync soldCount for product ID: {}", productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        
        Long totalSold = orderItemRepository.calculateTotalSoldByProductId(productId);
        int newSoldCount = (totalSold != null) ? totalSold.intValue() : 0;
        
        Integer currentSoldCount = product.getSoldCount() != null ? product.getSoldCount() : 0;
        if (!currentSoldCount.equals(newSoldCount)) {
            int oldSoldCount = currentSoldCount;
            product.setSoldCount(newSoldCount);
            productRepository.save(product);
            
            log.info("Updated product ID {} soldCount: {} -> {}", 
                     productId, oldSoldCount, newSoldCount);
        } else {
            log.info("Product ID {} soldCount unchanged: {}", productId, newSoldCount);
        }
    }
}
