package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.PlatformFee;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.repository.PlatformFeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Service for handling seller payment settlements
 * After orders are completed and return period expires, admin transfers money to sellers
 */
@Service
@Transactional
public class SettlementService {
    
    private static final Logger logger = LoggerFactory.getLogger(SettlementService.class);
    private static final int DEFAULT_RETURN_PERIOD_DAYS = 7; // Default 7 days return period
    
    private final OrderRepository orderRepository;
    private final PlatformFeeRepository platformFeeRepository;
    private final WalletService walletService;
    
    public SettlementService(OrderRepository orderRepository,
                            PlatformFeeRepository platformFeeRepository,
                            WalletService walletService) {
        this.orderRepository = orderRepository;
        this.platformFeeRepository = platformFeeRepository;
        this.walletService = walletService;
    }
    
    /**
     * Find orders eligible for settlement
     * - Status = COMPLETED
     * - Updated at least {returnPeriodDays} days ago
     * - Not yet settled (settled_at is null)
     * - Payment status = PAID
     */
    public List<Order> findOrdersEligibleForSettlement(int returnPeriodDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(returnPeriodDays);
        
        logger.info("Finding orders eligible for settlement (completed before: {})", cutoffDate);
        
        // Get all completed orders
        List<Order> completedOrders = orderRepository.findByStatus(Order.OrderStatus.COMPLETED);
        
        // Filter orders that:
        // 1. Were updated before cutoff date (completed at least returnPeriodDays ago)
        // 2. Payment status is PAID
        // 3. Not yet settled (we'll track this with a manual check since settled_at might not exist yet)
        List<Order> eligibleOrders = completedOrders.stream()
                .filter(order -> {
                    if (order.getUpdatedAt() == null) return false;
                    
                    LocalDateTime orderUpdated = order.getUpdatedAt().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    
                    return orderUpdated.isBefore(cutoffDate) 
                           && order.getPaymentStatus() == Order.PaymentStatus.PAID;
                })
                .toList();
        
        logger.info("Found {} orders eligible for settlement", eligibleOrders.size());
        
        return eligibleOrders;
    }
    
    /**
     * Settle a single order - transfer money from admin wallet to seller wallet
     */
    public void settleOrder(Order order) {
        logger.info("Settling order: {}", order.getId());
        
        if (order.getShop() == null || order.getShop().getOwner() == null) {
            throw new IllegalStateException("Order must have a shop with owner");
        }
        
        // Get seller ID from shop owner
        Long sellerId = order.getShop().getOwner().getId();
        
        // Get platform fee for this order
        PlatformFee platformFee = platformFeeRepository.findByOrderId(order.getId())
                .orElse(null);
        
        BigDecimal feeAmount = (platformFee != null && platformFee.getFeeAmount() != null)
                ? platformFee.getFeeAmount()
                : BigDecimal.ZERO;
        
        logger.info("Order #{}: Total={}, Platform Fee={}", 
                   order.getId(), order.getTotalAmount(), feeAmount);
        
        // Calculate seller amount = order total - platform fee
        BigDecimal sellerAmount = order.getTotalAmount().subtract(feeAmount);
        
        if (sellerAmount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Seller amount is zero or negative for order {}, skipping settlement", order.getId());
            return;
        }
        
        // Transfer money from admin to seller
        try {
            walletService.transferFromAdminToSeller(
                sellerId,
                sellerAmount,
                order,
                feeAmount
            );
            
            logger.info("✅ Successfully settled order #{}: {} transferred to seller #{}",
                       order.getId(), sellerAmount, sellerId);
            
            // TODO: If you add settled_at field to Order entity, uncomment this:
            // order.setSettledAt(new Date());
            //  orderRepository.save(order);
            
        } catch (Exception e) {
            logger.error("❌ Failed to settle order {}: {}", order.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Batch settlement - settle all eligible orders
     * Returns number of successfully settled orders
     */
    public int settleBatch(int returnPeriodDays) {
        logger.info("Starting batch settlement (return period: {} days)", returnPeriodDays);
        
        List<Order> eligibleOrders = findOrdersEligibleForSettlement(returnPeriodDays);
        
        if (eligibleOrders.isEmpty()) {
            logger.info("No orders to settle");
            return 0;
        }
        
        int successCount = 0;
        int failCount = 0;
        
        for (Order order : eligibleOrders) {
            try {
                settleOrder(order);
                successCount++;
            } catch (Exception e) {
                logger.error("Failed to settle order {}: {}", order.getId(), e.getMessage());
                failCount++;
                // Continue processing other orders
            }
        }
        
        logger.info("Batch settlement completed: {} succeeded, {} failed out of {} total",
                   successCount, failCount, eligibleOrders.size());
        
        return successCount;
    }
    
    /**
     * Batch settlement with default return period (7 days)
     */
    public int settleBatch() {
        return settleBatch(DEFAULT_RETURN_PERIOD_DAYS);
    }
    
    /**
     * Get settlement summary information
     */
    public SettlementSummary getSettlementSummary(int returnPeriodDays) {
        List<Order> eligibleOrders = findOrdersEligibleForSettlement(returnPeriodDays);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalFees = BigDecimal.ZERO;
        
        for (Order order : eligibleOrders) {
            totalAmount = totalAmount.add(order.getTotalAmount());
            
            PlatformFee platformFee = platformFeeRepository.findByOrderId(order.getId())
                    .orElse(null);
            
            if (platformFee != null && platformFee.getFeeAmount() != null) {
                totalFees = totalFees.add(platformFee.getFeeAmount());
            }
        }
        
        SettlementSummary summary = new SettlementSummary();
        summary.setEligibleOrdersCount(eligibleOrders.size());
        summary.setTotalOrderAmount(totalAmount);
        summary.setTotalPlatformFees(totalFees);
        summary.setTotalSellerPayout(totalAmount.subtract(totalFees));
        summary.setReturnPeriodDays(returnPeriodDays);
        
        return summary;
    }
    
    /**
     * DTO for settlement summary
     */
    public static class SettlementSummary {
        private int eligibleOrdersCount;
        private BigDecimal totalOrderAmount;
        private BigDecimal totalPlatformFees;
        private BigDecimal totalSellerPayout;
        private int returnPeriodDays;
        
        // Getters and Setters
        public int getEligibleOrdersCount() {
            return eligibleOrdersCount;
        }
        
        public void setEligibleOrdersCount(int eligibleOrdersCount) {
            this.eligibleOrdersCount = eligibleOrdersCount;
        }
        
        public BigDecimal getTotalOrderAmount() {
            return totalOrderAmount;
        }
        
        public void setTotalOrderAmount(BigDecimal totalOrderAmount) {
            this.totalOrderAmount = totalOrderAmount;
        }
        
        public BigDecimal getTotalPlatformFees() {
            return totalPlatformFees;
        }
        
        public void setTotalPlatformFees(BigDecimal totalPlatformFees) {
            this.totalPlatformFees = totalPlatformFees;
        }
        
        public BigDecimal getTotalSellerPayout() {
            return totalSellerPayout;
        }
        
        public void setTotalSellerPayout(BigDecimal totalSellerPayout) {
            this.totalSellerPayout = totalSellerPayout;
        }
        
        public int getReturnPeriodDays() {
            return returnPeriodDays;
        }
        
        public void setReturnPeriodDays(int returnPeriodDays) {
            this.returnPeriodDays = returnPeriodDays;
        }
    }
}
