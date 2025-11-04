package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.repository.OrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderCleanupService {
    private final OrderRepository orderRepository;

    public OrderCleanupService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Runs every 5 minutes
    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void cleanUnpaidMomoOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
        List<Order> oldUnpaidOrders = orderRepository.findUnpaidMomoOrdersBefore(cutoff);
        if (!oldUnpaidOrders.isEmpty()) {
            orderRepository.deleteAll(oldUnpaidOrders);
            System.out.println("[OrderCleanupService] Deleted " + oldUnpaidOrders.size() + " unpaid MoMo orders older than 5 minutes.");
        }
    }
}
