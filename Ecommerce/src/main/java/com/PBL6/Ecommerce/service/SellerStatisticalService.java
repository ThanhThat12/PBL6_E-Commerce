package com.PBL6.Ecommerce.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.PBL6.Ecommerce.constant.OrderStatus;
import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.dto.seller.CustomerStatsDTO;
import com.PBL6.Ecommerce.dto.seller.OrderStatusDistributionDTO;
import com.PBL6.Ecommerce.dto.seller.SalesDataDTO;
import com.PBL6.Ecommerce.dto.seller.TopProductDTO;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.PBL6.Ecommerce.repository.UserRepository;

@Service
public class SellerStatisticalService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ShopRepository shopRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * Get revenue data by date range
     */
    public List<SalesDataDTO> getRevenueData(String username, LocalDate startDate, LocalDate endDate) {
        Shop shop = getSellerShop(username);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Order> orders = orderRepository.findByShopIdAndStatusAndCreatedAtBetween(
            shop.getId(), OrderStatus.COMPLETED, startDateTime, endDateTime
        );
        
        // Group by date
        Map<LocalDate, SalesDataDTO> dataMap = new HashMap<>();
        for (Order order : orders) {
            LocalDate date = order.getCreatedAt().toLocalDate();
            SalesDataDTO data = dataMap.getOrDefault(date, new SalesDataDTO(date, 0L, BigDecimal.ZERO));
            data.setQuantity(data.getQuantity() + 1);
            data.setRevenue(data.getRevenue().add(order.getTotalAmount()));
            dataMap.put(date, data);
        }
        
        return new ArrayList<>(dataMap.values());
    }
    
    /**
     * Get sales quantity data
     */
    public List<SalesDataDTO> getSalesData(String username, LocalDate startDate, LocalDate endDate) {
        // Reuse revenue data method
        return getRevenueData(username, startDate, endDate);
    }
    
    /**
     * Get top products with date filter
     */
    public List<TopProductDTO> getTopProducts(String username, LocalDate startDate, LocalDate endDate, int limit) {
        Shop shop = getSellerShop(username);
        
        // Get all products and manually calculate from orders
        @SuppressWarnings("unused")
        List<Order> orders = orderRepository.findByShopIdAndStatus(shop.getId(), OrderStatus.COMPLETED);
        
        // TODO: Implement proper aggregation
        // For now, return empty list as placeholder
        return new ArrayList<>();
    }
    
    /**
     * Get customer analytics
     */
    public CustomerStatsDTO getCustomerAnalytics(String username) {
        Shop shop = getSellerShop(username);
        
        List<Order> orders = orderRepository.findByShopId(shop.getId());
        
        // Count unique customers
        long totalCustomers = orders.stream()
            .map(order -> order.getUser().getId())
            .distinct()
            .count();
        
        // Calculate average order value
        BigDecimal totalRevenue = orders.stream()
            .filter(order -> OrderStatus.COMPLETED.equals(OrderStatus.valueOf(order.getStatus().name())))
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long completedOrders = orders.stream()
            .filter(order -> OrderStatus.COMPLETED.equals(OrderStatus.valueOf(order.getStatus().name())))
            .count();
        
        BigDecimal avgOrderValue = completedOrders > 0 
            ? totalRevenue.divide(BigDecimal.valueOf(completedOrders), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        // New vs returning customers (simplified)
        long newCustomers = totalCustomers; // Placeholder
        long returningCustomers = 0L; // Placeholder
        
        return new CustomerStatsDTO(totalCustomers, newCustomers, returningCustomers, avgOrderValue);
    }
    
    /**
     * Get order status distribution
     */
    public List<OrderStatusDistributionDTO> getOrderStatusDistribution(String username) {
        Shop shop = getSellerShop(username);
        
        List<Order> orders = orderRepository.findByShopId(shop.getId());
        long total = orders.size();
        
        if (total == 0) {
            return new ArrayList<>();
        }
        
        // Count by status
        Map<Order.OrderStatus, Long> statusCounts = orders.stream()
            .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
        
        // Convert to DTO
        List<OrderStatusDistributionDTO> result = new ArrayList<>();
        for (Map.Entry<Order.OrderStatus, Long> entry : statusCounts.entrySet()) {
            double percentage = (entry.getValue() * 100.0) / total;
            result.add(new OrderStatusDistributionDTO(
                entry.getKey().name(),
                entry.getValue(),
                Math.round(percentage * 100.0) / 100.0
            ));
        }
        
        return result;
    }
    
    /**
     * Helper: Get seller's shop
     */
    private Shop getSellerShop(String username) {
        User user = userRepository.findOneByUsername(username)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        if (user.getRole() != com.PBL6.Ecommerce.domain.Role.SELLER) {
            throw new RuntimeException("Người dùng không phải là seller");
        }
        
        return shopRepository.findByOwner(user)
            .orElseThrow(() -> new RuntimeException("Seller chưa có shop"));
    }
}
