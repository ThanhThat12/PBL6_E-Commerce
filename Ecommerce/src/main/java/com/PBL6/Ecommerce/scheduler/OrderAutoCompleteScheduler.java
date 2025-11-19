package com.PBL6.Ecommerce.scheduler;

import com.PBL6.Ecommerce.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler để tự động hoàn thành đơn hàng sau 1 ngày kể từ khi chuyển sang SHIPPING
 */
@Component
public class OrderAutoCompleteScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderAutoCompleteScheduler.class);
    
    private final OrderService orderService;

    public OrderAutoCompleteScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Chạy mỗi 1 giờ để kiểm tra và tự động hoàn thành đơn hàng
     * Cron: 0 0 * * * * = Chạy vào phút 0 của mỗi giờ
     */
    @Scheduled(cron = "0 0 * * * *")
    public void autoCompleteShippingOrders() {
        logger.info("=== Starting auto-complete shipping orders task ===");
        try {
            orderService.autoCompleteShippingOrders();
            logger.info("=== Auto-complete task completed successfully ===");
        } catch (Exception e) {
            logger.error("Error during auto-complete task: {}", e.getMessage(), e);
        }
    }
}
