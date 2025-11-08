package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.dto.seller.CustomerDTO;
import com.PBL6.Ecommerce.dto.seller.CustomerDetailDTO;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SellerCustomerService {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Lấy danh sách khách hàng của seller (có phân trang)
     */
    public Page<CustomerDTO> getCustomers(String username, Pageable pageable) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Shop shop = shopRepository.findByOwner(owner)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        // Lấy tất cả orders của shop
        List<Order> orders = orderRepository.findByShopId(shop.getId());

        // Group by user_id để tính stats
        Map<Long, List<Order>> ordersByUser = orders.stream()
                .filter(o -> o.getUser() != null)
                .collect(Collectors.groupingBy(o -> o.getUser().getId()));

        // Convert to CustomerDTO
        List<CustomerDTO> customers = ordersByUser.entrySet().stream()
                .map(entry -> {
                    Long userId = entry.getKey();
                    List<Order> userOrders = entry.getValue();
                    
                    User user = userOrders.get(0).getUser();
                    
                    Long totalOrders = (long) userOrders.size();
                    
                    BigDecimal totalSpent = userOrders.stream()
                            .filter(o -> o.getStatus() == Order.OrderStatus.COMPLETED)
                            .map(Order::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    LocalDateTime lastOrderDate = userOrders.stream()
                            .map(Order::getCreatedAt)
                            .max(LocalDateTime::compareTo)
                            .orElse(null);
                    
                    LocalDateTime joinedDate = userOrders.stream()
                            .map(Order::getCreatedAt)
                            .min(LocalDateTime::compareTo)
                            .orElse(null);
                    
                    return new CustomerDTO(
                            userId,
                            user.getFullName(),
                            user.getEmail(),
                            user.getPhoneNumber(),
                            user.getAvatarUrl(),
                            totalOrders,
                            totalSpent,
                            lastOrderDate,
                            joinedDate
                    );
                })
                .sorted((c1, c2) -> c2.getTotalSpent().compareTo(c1.getTotalSpent())) // Sort by totalSpent DESC
                .collect(Collectors.toList());

        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), customers.size());
        List<CustomerDTO> pageContent = customers.subList(start, end);

        return new PageImpl<>(pageContent, pageable, customers.size());
    }

    /**
     * Lấy chi tiết khách hàng
     */
    public CustomerDetailDTO getCustomerDetail(String username, Long customerId) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Shop shop = shopRepository.findByOwner(owner)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Lấy orders của customer tại shop này
        List<Order> orders = orderRepository.findByShopId(shop.getId()).stream()
                .filter(o -> o.getUser() != null && o.getUser().getId().equals(customerId))
                .collect(Collectors.toList());

        if (orders.isEmpty()) {
            throw new RuntimeException("Customer has no orders in this shop");
        }

        // Calculate statistics
        Long totalOrders = (long) orders.size();
        
        Long completedOrders = orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.COMPLETED)
                .count();
        
        Long cancelledOrders = orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.CANCELLED)
                .count();
        
        BigDecimal totalSpent = orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.COMPLETED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageOrderValue = completedOrders > 0
                ? totalSpent.divide(BigDecimal.valueOf(completedOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        LocalDateTime firstOrderDate = orders.stream()
                .map(Order::getCreatedAt)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        
        LocalDateTime lastOrderDate = orders.stream()
                .map(Order::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        
        // Get recent orders (top 5)
        List<CustomerDetailDTO.CustomerOrderSummary> recentOrders = orders.stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(5)
                .map(order -> new CustomerDetailDTO.CustomerOrderSummary(
                        order.getId(),
                        order.getCreatedAt(),
                        order.getStatus().toString(),
                        order.getTotalAmount(),
                        order.getOrderItems().size()
                ))
                .collect(Collectors.toList());

        // Build DTO
        CustomerDetailDTO detail = new CustomerDetailDTO();
        detail.setUserId(customer.getId());
        detail.setFullName(customer.getFullName());
        detail.setEmail(customer.getEmail());
        detail.setPhoneNumber(customer.getPhoneNumber());
        detail.setAvatarUrl(customer.getAvatarUrl());
        detail.setAddress(null); // User entity doesn't have address field
        detail.setTotalOrders(totalOrders);
        detail.setCompletedOrders(completedOrders);
        detail.setCancelledOrders(cancelledOrders);
        detail.setTotalSpent(totalSpent);
        detail.setAverageOrderValue(averageOrderValue);
        detail.setFirstOrderDate(firstOrderDate);
        detail.setLastOrderDate(lastOrderDate);
        detail.setJoinedDate(customer.getCreatedAt());
        detail.setRecentOrders(recentOrders);

        return detail;
    }

    /**
     * Lấy lịch sử đơn hàng của một khách hàng (có phân trang)
     */
    public Page<Order> getCustomerOrders(String username, Long customerId, Pageable pageable) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Shop shop = shopRepository.findByOwner(owner)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        // Verify customer exists
        userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Lấy tất cả orders của customer tại shop này
        List<Order> orders = orderRepository.findByShopId(shop.getId()).stream()
                .filter(o -> o.getUser() != null && o.getUser().getId().equals(customerId))
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .collect(Collectors.toList());

        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), orders.size());
        List<Order> pageContent = orders.subList(start, end);

        return new PageImpl<>(pageContent, pageable, orders.size());
    }

    /**
     * Lấy thống kê khách hàng tổng quan
     */
    public Map<String, Object> getCustomerStats(String username) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Shop shop = shopRepository.findByOwner(owner)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        List<Order> orders = orderRepository.findByShopId(shop.getId());

        // Total unique customers
        Long totalCustomers = orders.stream()
                .filter(o -> o.getUser() != null)
                .map(o -> o.getUser().getId())
                .distinct()
                .count();

        // New customers (first order in last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        Map<Long, LocalDateTime> firstOrderDates = orders.stream()
                .filter(o -> o.getUser() != null)
                .collect(Collectors.groupingBy(
                        o -> o.getUser().getId(),
                        Collectors.mapping(Order::getCreatedAt, 
                                Collectors.minBy(LocalDateTime::compareTo))
                ))
                .entrySet().stream()
                .filter(e -> e.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));

        Long newCustomers = firstOrderDates.values().stream()
                .filter(date -> date.isAfter(thirtyDaysAgo))
                .count();

        // Returning customers (>1 order)
        Long returningCustomers = orders.stream()
                .filter(o -> o.getUser() != null)
                .collect(Collectors.groupingBy(o -> o.getUser().getId(), Collectors.counting()))
                .values().stream()
                .filter(count -> count > 1)
                .count();

        // Average order value
        BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.COMPLETED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long completedOrdersCount = orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.COMPLETED)
                .count();

        BigDecimal avgOrderValue = completedOrdersCount > 0
                ? totalRevenue.divide(BigDecimal.valueOf(completedOrdersCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Build response
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCustomers", totalCustomers);
        stats.put("newCustomers", newCustomers);
        stats.put("returningCustomers", returningCustomers);
        stats.put("averageOrderValue", avgOrderValue);
        stats.put("totalRevenue", totalRevenue);

        return stats;
    }
}
