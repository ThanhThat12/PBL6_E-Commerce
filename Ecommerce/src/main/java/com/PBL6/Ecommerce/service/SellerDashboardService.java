package com.PBL6.Ecommerce.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.PBL6.Ecommerce.constant.OrderStatus;
import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.OrderDTO;
import com.PBL6.Ecommerce.dto.seller.DashboardStatsDTO;
import com.PBL6.Ecommerce.dto.seller.RevenueDataDTO;
import com.PBL6.Ecommerce.dto.seller.TopProductDTO;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.PBL6.Ecommerce.repository.UserRepository;

/**
 * Service for Seller Dashboard
 * Provides statistics and analytics for seller dashboard
 */
@Service
public class SellerDashboardService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;
    
    public SellerDashboardService(OrderRepository orderRepository,
                                 ProductRepository productRepository,
                                 ShopRepository shopRepository,
                                 UserRepository userRepository,
                                 OrderService orderService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.orderService = orderService;
    }
    
    /**
     * Get dashboard statistics overview
     * @param username Seller username
     * @return DashboardStatsDTO with total revenue, orders, products, customers
     */
    public DashboardStatsDTO getDashboardStats(String username) {
        // Get seller's shop
        User seller = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        Shop shop = shopRepository.findByOwnerId(seller.getId())
            .orElseThrow(() -> new RuntimeException("Shop not found for seller: " + username));
        
        // Get all completed orders for revenue calculation
        List<Order> completedOrders = orderRepository.findByShopIdAndStatus(
            shop.getId(), OrderStatus.COMPLETED);
        
        // Calculate total revenue
        BigDecimal totalRevenue = completedOrders.stream()
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Get total orders (all statuses)
        Long totalOrders = orderRepository.countByShopId(shop.getId());
        
        // Get total products
        Long totalProducts = productRepository.countByShopId(shop.getId());
        
        // Get total unique customers (distinct user_id from orders)
        Long totalCustomers = orderRepository.countDistinctUsersByShopId(shop.getId());
        
        DashboardStatsDTO stats = new DashboardStatsDTO(
            totalRevenue, totalOrders, totalProducts, totalCustomers
        );
        
        // Calculate revenue change (optional - compare with previous period)
        calculateRevenueChange(stats, shop.getId());
        
        return stats;
    }
    
    /**
     * Get revenue statistics by time range
     * @param username Seller username
     * @param timeRange "week", "month", "year"
     * @return List of RevenueDataDTO
     */
    public List<RevenueDataDTO> getRevenueStats(String username, String timeRange) {
        User seller = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        Shop shop = shopRepository.findByOwnerId(seller.getId())
            .orElseThrow(() -> new RuntimeException("Shop not found for seller: " + username));
        
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate;
        
        // Determine time range
        switch (timeRange.toLowerCase()) {
            case "week":
                startDate = endDate.minus(7, ChronoUnit.DAYS);
                break;
            case "month":
                startDate = endDate.minus(30, ChronoUnit.DAYS);
                break;
            case "year":
                startDate = endDate.minus(365, ChronoUnit.DAYS);
                break;
            default:
                startDate = endDate.minus(30, ChronoUnit.DAYS); // default to month
        }
        
        // Get orders in time range
        List<Order> orders = orderRepository.findByShopIdAndStatusAndCreatedAtBetween(
            shop.getId(), OrderStatus.COMPLETED, startDate, endDate);
        
        // Group by date and calculate revenue
        return groupOrdersByDate(orders, timeRange);
    }
    
    /**
     * Get recent orders for dashboard
     * @param username Seller username
     * @param limit Number of orders to return
     * @return List of OrderDTO
     */
    public List<OrderDTO> getRecentOrders(String username, int limit) {
        // Reuse existing OrderService method
        List<OrderDTO> allOrders = orderService.getSellerOrders(username);
        
        // Return only first N orders (already sorted by created_at desc)
        return allOrders.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Get top selling products
     * @param username Seller username
     * @param limit Number of products to return
     * @return List of TopProductDTO
     */
    public List<TopProductDTO> getTopProducts(String username, int limit) {
        User seller = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        Shop shop = shopRepository.findByOwnerId(seller.getId())
            .orElseThrow(() -> new RuntimeException("Shop not found for seller: " + username));
        
        // Get top products by sold count using Pageable for limit
        Pageable pageable = PageRequest.of(0, limit);
        List<com.PBL6.Ecommerce.domain.Product> products = 
            productRepository.findTopSellingProductsByShopId(shop.getId(), pageable);
        
        // Convert to TopProductDTO
        return products.stream()
            .map(product -> {
                TopProductDTO dto = new TopProductDTO();
                dto.setProductId(product.getId());
                dto.setProductName(product.getName());
                dto.setImageUrl(product.getMainImage());
                dto.setPrice(product.getBasePrice());
                
                // TODO: sold_count field chưa có trong ProductVariant
                // Tạm thời set = 0, cần thêm field sold_count vào ProductVariant entity
                dto.setSoldCount(0L);
                dto.setRevenue(BigDecimal.ZERO);
                
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    // Helper methods
    
    private void calculateRevenueChange(DashboardStatsDTO stats, Long shopId) {
        try {
            // Get current month revenue
            LocalDateTime currentMonthStart = LocalDateTime.now().withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0);
            LocalDateTime currentMonthEnd = LocalDateTime.now();
            
            List<Order> currentMonthOrders = orderRepository
                .findByShopIdAndStatusAndCreatedAtBetween(
                    shopId, OrderStatus.COMPLETED, currentMonthStart, currentMonthEnd);
            
            BigDecimal currentRevenue = currentMonthOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Get previous month revenue
            LocalDateTime prevMonthStart = currentMonthStart.minus(1, ChronoUnit.MONTHS);
            LocalDateTime prevMonthEnd = currentMonthStart.minus(1, ChronoUnit.SECONDS);
            
            List<Order> prevMonthOrders = orderRepository
                .findByShopIdAndStatusAndCreatedAtBetween(
                    shopId, OrderStatus.COMPLETED, prevMonthStart, prevMonthEnd);
            
            BigDecimal prevRevenue = prevMonthOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Calculate percentage change
            if (prevRevenue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal change = currentRevenue.subtract(prevRevenue)
                    .divide(prevRevenue, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
                stats.setRevenueChange(change);
            }
            
            // Calculate order count change
            Long orderChange = (long) (currentMonthOrders.size() - prevMonthOrders.size());
            stats.setOrderChange(orderChange);
            
        } catch (Exception e) {
            // If calculation fails, just set to 0
            stats.setRevenueChange(BigDecimal.ZERO);
            stats.setOrderChange(0L);
        }
    }
    
    private List<RevenueDataDTO> groupOrdersByDate(List<Order> orders, String timeRange) {
        List<RevenueDataDTO> result = new ArrayList<>();
        
        // Group orders by date
        orders.stream()
            .collect(Collectors.groupingBy(
                order -> order.getCreatedAt().toLocalDate()
            ))
            .forEach((date, orderList) -> {
                BigDecimal revenue = orderList.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                result.add(new RevenueDataDTO(date, revenue, (long) orderList.size()));
            });
        
        // Sort by date
        result.sort((a, b) -> a.getDate().compareTo(b.getDate()));
        
        return result;
    }
}
