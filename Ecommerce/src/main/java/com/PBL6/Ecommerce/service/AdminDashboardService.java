package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.Role;
import com.PBL6.Ecommerce.domain.dto.admin.*;
import com.PBL6.Ecommerce.repository.CategoryRepository;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 1. Sales Overview - Get overall dashboard statistics
     */
    @Transactional(readOnly = true)
    public AdminDashboardStatsDTO getDashboardStats() {
        // Current stats
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        Long totalOrders = orderRepository.count();
        Long activeCustomers = userRepository.countByRoleAndActivated(Role.BUYER, true);

        // Calculate conversion rate (completed orders / total orders * 100)
        Long completedOrders = orderRepository.countByStatus(Order.OrderStatus.COMPLETED);
        Double conversionRate = totalOrders > 0 
            ? (completedOrders.doubleValue() / totalOrders.doubleValue() * 100)
            : 0.0;

        // Calculate growth percentages (last 30 days vs previous 30 days)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        LocalDateTime sixtyDaysAgo = now.minusDays(60);

        // Revenue growth
        BigDecimal currentRevenue = orderRepository.calculateRevenueByDateRange(thirtyDaysAgo, now);
        BigDecimal previousRevenue = orderRepository.calculateRevenueByDateRange(sixtyDaysAgo, thirtyDaysAgo);
        Double revenueGrowth = calculateGrowthPercentage(currentRevenue, previousRevenue);

        // Orders growth
        Long currentOrders = orderRepository.countByCreatedAtBetween(thirtyDaysAgo, now);
        Long previousOrders = orderRepository.countByCreatedAtBetween(sixtyDaysAgo, thirtyDaysAgo);
        Double ordersGrowth = calculateGrowthPercentage(currentOrders, previousOrders);

        // Customers growth
        Long currentCustomers = userRepository.countByRoleAndCreatedAtBetween(Role.BUYER, thirtyDaysAgo, now);
        Long previousCustomers = userRepository.countByRoleAndCreatedAtBetween(Role.BUYER, sixtyDaysAgo, thirtyDaysAgo);
        Double customersGrowth = calculateGrowthPercentage(currentCustomers, previousCustomers);

        // Conversion growth
        Double previousConversionRate = previousOrders > 0 
            ? (orderRepository.countByStatus(Order.OrderStatus.COMPLETED).doubleValue() / previousOrders.doubleValue() * 100)
            : 0.0;
        Double conversionGrowth = previousConversionRate > 0 
            ? ((conversionRate - previousConversionRate) / previousConversionRate * 100)
            : 0.0;

        return new AdminDashboardStatsDTO(
            totalRevenue,
            totalOrders,
            activeCustomers,
            Math.round(conversionRate * 10.0) / 10.0,
            Math.round(revenueGrowth * 10.0) / 10.0,
            Math.round(ordersGrowth * 10.0) / 10.0,
            Math.round(customersGrowth * 10.0) / 10.0,
            Math.round(conversionGrowth * 10.0) / 10.0
        );
    }

    /**
     * 2. Sales by Category - Get sales breakdown by category
     */
    @Transactional(readOnly = true)
    public List<SalesByCategoryDTO> getSalesByCategory() {
        List<AdminCategoryStatsDTO> categoryStats = categoryRepository.getCategoriesWithStats();

        return categoryStats.stream()
            .map(stats -> new SalesByCategoryDTO(
                stats.getCategoryName(),
                stats.getTotalRevenue() != null ? stats.getTotalRevenue() : BigDecimal.ZERO,
                stats.getOrderCount() != null ? stats.getOrderCount() : 0L
            ))
            .collect(Collectors.toList());
    }

    /**
     * 3. Top Selling Products - Get top selling products
     */
    @Transactional(readOnly = true)
    public List<TopSellingProductDTO> getTopSellingProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> topProducts = orderRepository.findTopSellingProducts(pageable);

        return topProducts.stream()
            .map(row -> {
                Long productId = ((Number) row[0]).longValue();
                String productName = (String) row[1];
                String categoryName = row[2] != null ? (String) row[2] : "Uncategorized";
                String mainImage = row[3] != null ? (String) row[3] : null;
                Long quantitySold = ((Number) row[4]).longValue();
                BigDecimal totalRevenue = (BigDecimal) row[5];
                Boolean isActive = (Boolean) row[6];

                String status = isActive ? "Active" : "Inactive";

                return new TopSellingProductDTO(
                    productId,
                    productName,
                    categoryName,
                    mainImage,
                    quantitySold,
                    totalRevenue,
                    status
                );
            })
            .collect(Collectors.toList());
    }

    /**
     * 4. Recent Orders - Get recent orders
     */
    @Transactional(readOnly = true)
    public List<RecentOrderDTO> getRecentOrders(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Order> recentOrders = orderRepository.findRecentOrders(pageable);

        return recentOrders.stream()
            .map(order -> {
                RecentOrderDTO dto = new RecentOrderDTO();
                dto.setId(order.getId());
                dto.setCustomerName(order.getUser().getFullName());
                dto.setCustomerEmail(order.getUser().getEmail());
                dto.setOrderDate(order.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                dto.setTotalAmount(order.getTotalAmount());
                dto.setStatus(order.getStatus());
                return dto;
            })
            .collect(Collectors.toList());
    }

    /**
     * 5. Revenue Chart - Get revenue data by month (last 6 months)
     */
    @Transactional(readOnly = true)
    public List<RevenueChartDTO> getRevenueChart(int months) {
        List<RevenueChartDTO> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'Th'M");
        
        LocalDateTime now = LocalDateTime.now();
        
        // Lấy dữ liệu cho N tháng gần nhất
        for (int i = months - 1; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime monthEnd;
            
            if (i == 0) {
                // Tháng hiện tại: lấy đến hiện tại
                monthEnd = now;
            } else {
                // Tháng trước: lấy đến cuối tháng
                monthEnd = monthStart.plusMonths(1).minusNanos(1);
            }
            
            // Tính doanh thu và số đơn hàng trong khoảng thời gian này
            BigDecimal revenue = orderRepository.calculateCompletedRevenueByDateRange(monthStart, monthEnd);
            Long orderCount = orderRepository.countCompletedOrdersByDateRange(monthStart, monthEnd);
            
            result.add(new RevenueChartDTO(
                monthStart.format(formatter),
                revenue != null ? revenue : BigDecimal.ZERO,
                orderCount != null ? orderCount : 0L
            ));
        }
        
        return result;
    }

    // Helper methods
    private Double calculateGrowthPercentage(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        if (current == null) {
            current = BigDecimal.ZERO;
        }
        return current.subtract(previous)
            .divide(previous, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .doubleValue();
    }

    private Double calculateGrowthPercentage(Long current, Long previous) {
        if (previous == null || previous == 0) {
            return current != null && current > 0 ? 100.0 : 0.0;
        }
        if (current == null) {
            current = 0L;
        }
        return ((current - previous) / (double) previous) * 100;
    }
}
