package com.PBL6.Ecommerce.scheduler;

import com.PBL6.Ecommerce.domain.entity.order.Order;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Scheduler to automatically transfer funds to sellers after order completion
 * Runs every 5 minutes to check for eligible orders
 */
@Component
public class WalletTransactionScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(WalletTransactionScheduler.class);
    
    // Waiting period in minutes after order completion before transferring to seller
    // Change this value to adjust the waiting period (e.g., 1440 for 24 hours, 2880 for 48 hours)
    private static final int WAITING_PERIOD_MINUTES = 2; // Currently set to 2 minutes for testing
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private WalletService walletService;
    
    //  * Scheduled task to process seller payments
    //  * Runs every 5 minutes: cron = "0 */5 * * * *"
    //  * For testing, runs every 30 seconds: cron = "*/30 * * * * *"
    @Scheduled(cron = "*/30 * * * * *") // Every 30 seconds for testing
    // @Scheduled(cron = "0 */5 * * * *") // Every 5 minutes for production
    public void processSellerPayments() {
        try {
            logger.info("🔄 [Scheduler] Starting seller payment processing...");
            
            // Calculate cutoff time (current time - waiting period)
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, -WAITING_PERIOD_MINUTES);
            Date cutoffTime = calendar.getTime();
            
            logger.info("📅 [Scheduler] Cutoff time: {} (waiting period: {} minutes)", 
                       cutoffTime, WAITING_PERIOD_MINUTES);
            
            // Find completed orders ready for payment
            List<Order> eligibleOrders = orderRepository.findCompletedOrdersReadyForPayment(cutoffTime);
            
            if (eligibleOrders.isEmpty()) {
                logger.info("✅ [Scheduler] No orders to process at this time");
                return;
            }
            
            logger.info("📦 [Scheduler] Found {} orders ready for seller payment", eligibleOrders.size());
            
            // Process each order
            int successCount = 0;
            int failCount = 0;
            
            for (Order order : eligibleOrders) {
                try {
                    logger.info("💰 [Scheduler] Processing order #{}...", order.getId());
                    
                    // Transfer funds to seller (90% to seller, 10% platform fee)
                    walletService.transferFromAdminToSeller(
                        order.getShop().getOwner().getId(),
                        order.getTotalAmount(),
                        order
                    );
                    
                    successCount++;
                    logger.info("✅ [Scheduler] Successfully processed order #{}", order.getId());
                    
                } catch (Exception e) {
                    failCount++;
                    logger.error("❌ [Scheduler] Failed to process order #{}: {}", 
                               order.getId(), e.getMessage(), e);
                }
            }
            
            logger.info("🎯 [Scheduler] Completed seller payment processing. " +
                       "Success: {}, Failed: {}, Total: {}",
                       successCount, failCount, eligibleOrders.size());
            
        } catch (Exception e) {
            logger.error("❌ [Scheduler] Error in seller payment processing: {}", e.getMessage(), e);
        }
    }
}
