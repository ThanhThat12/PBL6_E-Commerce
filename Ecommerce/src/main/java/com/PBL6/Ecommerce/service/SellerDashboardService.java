package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.dto.seller.SellerDashboardStatsDTO;
import com.PBL6.Ecommerce.domain.entity.order.Order;
import com.PBL6.Ecommerce.domain.entity.shop.Shop;
import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SellerDashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    /**
     * Get seller dashboard statistics
     */
    @Transactional(readOnly = true)
    public SellerDashboardStatsDTO getDashboardStats(Authentication authentication) {
        String username = authentication.getName();
        User seller = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy seller"));

        Shop shop = shopRepository.findByOwnerId(seller.getId())
            .orElseThrow(() -> new RuntimeException("Seller chưa có shop"));

        // Current stats
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue(shop.getId());
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        Long totalOrders = orderRepository.countCompletedOrders(shop.getId());
        Long totalProducts = productRepository.countByShopId(shop.getId());
        Long totalCustomers = (long) orderRepository.findDistinctCustomersByShop(shop).size();

        // Calculate growth percentages (last 30 days vs previous 30 days)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        LocalDateTime sixtyDaysAgo = now.minusDays(60);

        // Revenue growth
        BigDecimal currentRevenue = calculateRevenueByDateRange(shop.getId(), thirtyDaysAgo, now);
        BigDecimal previousRevenue = calculateRevenueByDateRange(shop.getId(), sixtyDaysAgo, thirtyDaysAgo);
        Double revenueGrowth = calculateGrowthPercentage(currentRevenue, previousRevenue);

        // Orders growth
        Long currentOrders = countOrdersByDateRange(shop.getId(), thirtyDaysAgo, now);
        Long previousOrders = countOrdersByDateRange(shop.getId(), sixtyDaysAgo, thirtyDaysAgo);
        Double ordersGrowth = calculateGrowthPercentage(currentOrders, previousOrders);

        // Products growth
        Long currentProducts = productRepository.countByShopIdAndCreatedAtBetween(shop.getId(), thirtyDaysAgo, now);
        Long previousProducts = productRepository.countByShopIdAndCreatedAtBetween(shop.getId(), sixtyDaysAgo, thirtyDaysAgo);
        Double productsGrowth = calculateGrowthPercentage(currentProducts, previousProducts);

        // Customers growth
        Long currentCustomers = countCustomersByDateRange(shop, thirtyDaysAgo, now);
        Long previousCustomers = countCustomersByDateRange(shop, sixtyDaysAgo, thirtyDaysAgo);
        Double customersGrowth = calculateGrowthPercentage(currentCustomers, previousCustomers);

        // Build DTO
        SellerDashboardStatsDTO stats = new SellerDashboardStatsDTO();
        stats.setTotalRevenue(totalRevenue);
        stats.setTotalOrders(totalOrders);
        stats.setTotalProducts(totalProducts);
        stats.setTotalCustomers(totalCustomers.longValue());
        
        // Set growth percentages
        stats.setRevenueGrowth(Math.round(revenueGrowth * 10.0) / 10.0);
        stats.setOrdersGrowth(Math.round(ordersGrowth * 10.0) / 10.0);
        stats.setProductsGrowth(Math.round(productsGrowth * 10.0) / 10.0);
        stats.setCustomersGrowth(Math.round(customersGrowth * 10.0) / 10.0);
        
        // Format trend strings
        stats.setOrdersTrend(formatTrend(ordersGrowth, "từ tháng trước"));
        stats.setProductsTrend(formatProductTrend(currentProducts));
        stats.setCustomersTrend(formatTrend(customersGrowth, "từ tháng trước"));

        return stats;
    }

    private BigDecimal calculateRevenueByDateRange(Long shopId, LocalDateTime start, LocalDateTime end) {
        BigDecimal revenue = orderRepository.calculateRevenueByShopAndDateRange(shopId, start, end);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    private Long countOrdersByDateRange(Long shopId, LocalDateTime start, LocalDateTime end) {
        return orderRepository.countByShopIdAndStatusAndCreatedAtBetween(
            shopId, Order.OrderStatus.COMPLETED, start, end);
    }

    private Long countCustomersByDateRange(Shop shop, LocalDateTime start, LocalDateTime end) {
        return orderRepository.countDistinctCustomersByShopAndDateRange(shop, start, end);
    }

    private Double calculateGrowthPercentage(Number current, Number previous) {
        if (previous == null || previous.doubleValue() == 0) {
            return current != null && current.doubleValue() > 0 ? 100.0 : 0.0;
        }
        return ((current.doubleValue() - previous.doubleValue()) / previous.doubleValue()) * 100;
    }

    private String formatTrend(Double growth, String suffix) {
        if (growth > 0) {
            return String.format("+%.1f%% %s", growth, suffix);
        } else if (growth < 0) {
            return String.format("%.1f%% %s", growth, suffix);
        }
        return "Không đổi";
    }

    private String formatProductTrend(Long newProducts) {
        if (newProducts > 0) {
            return String.format("+%d sản phẩm mới", newProducts);
        }
        return "Không có sản phẩm mới";
    }
}
