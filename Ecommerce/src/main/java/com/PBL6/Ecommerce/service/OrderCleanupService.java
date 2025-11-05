package com.PBL6.Ecommerce.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.repository.PlatformFeeRepository;
import com.PBL6.Ecommerce.repository.RefundRepository;
import com.PBL6.Ecommerce.repository.ShipmentRepository;
import com.PBL6.Ecommerce.repository.UserVouchersRepository;
import com.PBL6.Ecommerce.repository.WalletTransactionRepository;

@Service
public class OrderCleanupService {
    private static final Logger logger = LoggerFactory.getLogger(OrderCleanupService.class);
    
    private final OrderRepository orderRepository;
    private final PlatformFeeRepository platformFeeRepository;
    private final ShipmentRepository shipmentRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final RefundRepository refundRepository;
    private final UserVouchersRepository userVouchersRepository;

    public OrderCleanupService(
            OrderRepository orderRepository,
            PlatformFeeRepository platformFeeRepository,
            ShipmentRepository shipmentRepository,
            WalletTransactionRepository walletTransactionRepository,
            RefundRepository refundRepository,
            UserVouchersRepository userVouchersRepository) {
        this.orderRepository = orderRepository;
        this.platformFeeRepository = platformFeeRepository;
        this.shipmentRepository = shipmentRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.refundRepository = refundRepository;
        this.userVouchersRepository = userVouchersRepository;
    }

    // Runs every 5 minutes
    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void cleanUnpaidMomoOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
        List<Order> oldUnpaidOrders = orderRepository.findUnpaidMomoOrdersBefore(cutoff);
        
        if (oldUnpaidOrders.isEmpty()) {
            return;
        }
        
        logger.info("Found {} unpaid MoMo orders to clean up", oldUnpaidOrders.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (Order order : oldUnpaidOrders) {
            try {
                Long orderId = order.getId();
                logger.debug("Cleaning order ID: {}", orderId);
                
                // Xóa theo thứ tự ràng buộc khóa ngoại
                // 1. Xóa platform_fees trước
                platformFeeRepository.deleteByOrderId(orderId);
                platformFeeRepository.flush(); // Force immediate execution
                
                // 2. Xóa wallet_transactions liên quan
                walletTransactionRepository.deleteByRelatedOrderId(orderId);
                walletTransactionRepository.flush();
                
                // 3. Xóa refunds liên quan
                refundRepository.deleteByOrderId(orderId);
                refundRepository.flush();
                
                // 4. Xóa user_vouchers liên quan
                userVouchersRepository.deleteByOrderId(orderId);
                userVouchersRepository.flush();
                
                // 5. Xóa shipment nếu có (nếu không xóa, FK fk_order_shipment sẽ gây lỗi)
                if (order.getShipment() != null) {
                    shipmentRepository.deleteById(order.getShipment().getId());
                    shipmentRepository.flush();
                }
                
                // 6. Xóa order (order_items sẽ tự động xóa vì ON DELETE CASCADE)
                orderRepository.delete(order);
                orderRepository.flush();
                
                logger.info("Successfully deleted unpaid MoMo order: {}", orderId);
                successCount++;
            } catch (Exception e) {
                logger.error("Error deleting order {}: {}", order.getId(), e.getMessage(), e);
                failCount++;
                // Continue with next order instead of stopping entire cleanup
            }
        }
        
        logger.info("Cleanup completed! Success: {}, Failed: {}, Total: {}", 
                    successCount, failCount, oldUnpaidOrders.size());
    }
}
