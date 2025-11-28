package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.repository.OrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.PBL6.Ecommerce.repository.PlatformFeeRepository;
import com.PBL6.Ecommerce.repository.ShipmentRepository;
import com.PBL6.Ecommerce.repository.WalletTransactionRepository;
import com.PBL6.Ecommerce.repository.RefundRepository;
import com.PBL6.Ecommerce.repository.UserVouchersRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderCleanupService {
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
        for (Order order : oldUnpaidOrders) {
            // Xóa theo thứ tự ràng buộc khóa ngoại
            // 1. Xóa platform_fees trước
            platformFeeRepository.deleteByOrderId(order.getId());
            
            // 2. Xóa wallet_transactions liên quan
            walletTransactionRepository.deleteByRelatedOrderId(order.getId());
            
            // 3. Xóa refunds liên quan
            refundRepository.deleteByOrderId(order.getId());
            
            // 4. Xóa user_vouchers liên quan
            userVouchersRepository.deleteByOrderId(order.getId());
            
            // 5. Xóa shipment nếu có (nếu không xóa, FK fk_order_shipment sẽ gây lỗi)
            var shipment = shipmentRepository.findByOrderId(order.getId()).orElse(null);
            if (shipment != null) {
                shipmentRepository.deleteById(shipment.getId());
            }
            
            // 6. Xóa order (order_items sẽ tự động xóa vì ON DELETE CASCADE)
            orderRepository.delete(order);
            
            System.out.println("[OrderCleanupService] Deleted unpaid MoMo order: " + order.getId());
        }
        if (!oldUnpaidOrders.isEmpty()) {
            System.out.println("[OrderCleanupService] Cleanup completed! Deleted " + oldUnpaidOrders.size() + " unpaid MoMo orders and all related data.");
        }
    }
}
