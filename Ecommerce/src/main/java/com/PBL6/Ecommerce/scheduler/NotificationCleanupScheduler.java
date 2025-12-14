package com.PBL6.Ecommerce.scheduler;

import com.PBL6.Ecommerce.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduler để tự động xóa các thông báo cũ
 * - Xóa các thông báo đã đọc (isRead = true) và cũ hơn 5 phút
 */
@Component
public class NotificationCleanupScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationCleanupScheduler.class);
    private static final int CLEANUP_MINUTES = 5;
    
    private final NotificationRepository notificationRepository;

    public NotificationCleanupScheduler(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Chạy mỗi 1 phút để xóa thông báo cũ
     */
    @Scheduled(fixedRate = 60000) // 60 seconds = 1 minute
    @Transactional
    public void cleanupOldNotifications() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(CLEANUP_MINUTES);
            
            // Xóa các thông báo đã đọc và cũ hơn 5 phút
            int deletedCount = notificationRepository.deleteByIsReadTrueAndCreatedAtBefore(cutoffTime);
            
            if (deletedCount > 0) {
                logger.info("🗑️ Cleaned up {} old read notifications (older than {} minutes)", 
                           deletedCount, CLEANUP_MINUTES);
            }
            
        } catch (Exception e) {
            logger.error("❌ Error in notification cleanup scheduler: {}", e.getMessage(), e);
        }
    }
}
