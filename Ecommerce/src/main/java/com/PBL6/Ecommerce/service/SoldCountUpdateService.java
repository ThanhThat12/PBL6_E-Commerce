package com.PBL6.Ecommerce.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to update sold_count for products based on completed orders
 * Runs periodically to keep sold_count synchronized with actual order data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SoldCountUpdateService {

    private final ProductRepository productRepository;

    /**
     * Update sold_count for a specific product
     * Call this immediately after an order is completed
     * 
     * @param productId ID of the product to update
     */
    @Transactional
    public void updateSoldCountForProduct(Long productId) {
        try {
            productRepository.updateSoldCount(productId);
            log.info("Updated sold_count for product ID: {}", productId);
        } catch (Exception e) {
            log.error("Failed to update sold_count for product ID: {}", productId, e);
        }
    }

    /**
     * Update sold_count for all products
     * This is a batch operation for maintenance purposes
     */
    @Transactional
    public void updateAllSoldCounts() {
        try {
            long startTime = System.currentTimeMillis();
            productRepository.updateAllSoldCounts();
            long duration = System.currentTimeMillis() - startTime;
            log.info("Successfully updated sold_count for all products in {}ms", duration);
        } catch (Exception e) {
            log.error("Failed to update sold_count for all products", e);
        }
    }

    /**
     * Scheduled job to update sold_count for all products
     * Runs every day at 2:00 AM to keep data synchronized
     * 
     * Cron expression: "0 0 2 * * ?" = At 2:00 AM every day
     * You can also use: @Scheduled(fixedRate = 3600000) for hourly updates (1 hour = 3600000ms)
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void scheduledSoldCountUpdate() {
        log.info("Starting scheduled sold_count update for all products...");
        updateAllSoldCounts();
    }
}
