package com.PBL6.Ecommerce.scheduler;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.service.WalletService;
import com.PBL6.Ecommerce.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Scheduler để tự động chuyển tiền từ admin sang seller
 * - Admin giữ lại 10% hoa hồng
 * - Seller nhận 90% giá trị đơn hàng
 * - Chuyển tiền sau 2 phút kể từ khi admin nhận tiền
 */
@Component
public class SellerPayoutScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(SellerPayoutScheduler.class);
    private static final BigDecimal ADMIN_COMMISSION_RATE = new BigDecimal("0.10"); // 10%
    private static final int PAYOUT_DELAY_MINUTES = 2;
    
    private final OrderRepository orderRepository;
    private final WalletService walletService;
    private final NotificationService notificationService;

    public SellerPayoutScheduler(OrderRepository orderRepository, WalletService walletService, NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.walletService = walletService;
        this.notificationService = notificationService;
    }

    /**
     * Chạy mỗi 30 giây để kiểm tra các đơn hàng cần chuyển tiền cho seller
     */
    @Scheduled(fixedRate = 30000) // 30 seconds
    @Transactional
    public void processSellerPayouts() {
        try {
            logger.debug("🔄 Running seller payout check...");
            
            // Tìm các đơn hàng đã nhận tiền từ admin (depositToAdminWallet đã chạy)
            // Check từ createdAt đến hiện tại >= 2 phút
            LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(PAYOUT_DELAY_MINUTES);
            Date cutoffDate = Date.from(twoMinutesAgo.atZone(ZoneId.systemDefault()).toInstant());
            
            // Tìm đơn hàng PAID, có createdAt < 2 phút trước, chưa chuyển cho seller
            List<Order> ordersToPayout = orderRepository.findOrdersReadyForSellerPayout(cutoffDate);
            
            if (ordersToPayout.isEmpty()) {
                logger.debug("✅ No orders ready for seller payout");
                return;
            }
            
            logger.info("💰 Found {} orders ready for seller payout", ordersToPayout.size());
            
            for (Order order : ordersToPayout) {
                try {
                    // Kiểm tra đã đủ 2 phút từ createdAt
                    long minutesSinceCreation = java.time.Duration.between(
                        order.getCreatedAt().toInstant(),
                        new Date().toInstant()
                    ).toMinutes();
                    
                    if (minutesSinceCreation >= PAYOUT_DELAY_MINUTES) {
                        processSellerPayout(order);
                    }
                } catch (Exception e) {
                    logger.error("❌ Failed to process payout for order {}: {}", 
                                order.getId(), e.getMessage(), e);
                    // Continue with next order
                }
            }
            
        } catch (Exception e) {
            logger.error("❌ Error in seller payout scheduler: {}", e.getMessage(), e);
        }
    }

    /**
     * Xử lý chuyển tiền cho seller cho một đơn hàng cụ thể
     */
    @Transactional
    public void processSellerPayout(Order order) {
        logger.info("💵 Processing seller payout for order #{}", order.getId());
        
        BigDecimal orderAmount = order.getTotalAmount();
        Long sellerId = order.getShop().getOwner().getId();
        
        // Tính hoa hồng admin (10%)
        BigDecimal adminCommission = orderAmount.multiply(ADMIN_COMMISSION_RATE);
        
        // Số tiền seller nhận (90%)
        BigDecimal sellerAmount = orderAmount.subtract(adminCommission);
        
        logger.info("💰 Order #{}: Total={}, Admin Commission (10%)={}, Seller Amount (90%)={}", 
                   order.getId(), orderAmount, adminCommission, sellerAmount);
        
        try {
            // Chuyển tiền từ admin wallet sang seller wallet
            walletService.transferFromAdminToSeller(
                sellerId, 
                sellerAmount, 
                order,
                String.format("Thanh toán đơn hàng #%d (trừ 10%% phí dịch vụ)", order.getId())
            );
            
            // Không cần update order - WalletTransaction đã đánh dấu
            
            // Gửi thông báo cho seller
            String sellerMessage = String.format("Bạn đã nhận được tiền đơn hàng #%d (%.0f VNĐ)", 
                                                order.getId(), sellerAmount);
            notificationService.sendSellerNotification(sellerId, "PAYMENT_RECEIVED", sellerMessage, order.getId());
            
            logger.info("✅ Successfully paid out {} to seller #{} for order #{} (Admin kept {} as commission)", 
                       sellerAmount, sellerId, order.getId(), adminCommission);
            
        } catch (Exception e) {
            logger.error("❌ Failed to transfer funds to seller for order {}: {}", 
                        order.getId(), e.getMessage());
            throw e;
        }
    }
}
