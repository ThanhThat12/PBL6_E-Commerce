package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.ShopDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateShopDTO;
import com.PBL6.Ecommerce.domain.dto.ShopAnalyticsDTO;
import com.PBL6.Ecommerce.domain.dto.MonthlyRevenueDTO;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ShopService {
    
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public ShopService(ShopRepository shopRepository, UserRepository userRepository, OrderRepository orderRepository) {
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Lấy thông tin shop của seller
     * @param username - Username của seller từ JWT token
     * @return ShopDTO - Thông tin shop
     */
    public ShopDTO getSellerShop(String username) {
        // Tìm user theo username
        User user = userRepository.findOneByUsername(username)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Kiểm tra role SELLER
        if (user.getRole() != com.PBL6.Ecommerce.domain.Role.SELLER) {
            throw new RuntimeException("Người dùng không phải là seller");
        }

        // Tìm shop của seller
        Shop shop = shopRepository.findByOwner(user)
            .orElseThrow(() -> new RuntimeException("Seller chưa có shop"));

        // Convert sang DTO
        return convertToDTO(shop);
    }

    /**
     * Cập nhật thông tin shop
     * @param username - Username của seller từ JWT token
     * @param updateShopDTO - Dữ liệu cập nhật
     * @return ShopDTO - Thông tin shop sau khi cập nhật
     */
    public ShopDTO updateSellerShop(String username, UpdateShopDTO updateShopDTO) {
        // Tìm user theo username
        User user = userRepository.findOneByUsername(username)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Kiểm tra role SELLER
        if (user.getRole() != com.PBL6.Ecommerce.domain.Role.SELLER) {
            throw new RuntimeException("Người dùng không phải là seller");
        }

        // Tìm shop của seller
        Shop shop = shopRepository.findByOwner(user)
            .orElseThrow(() -> new RuntimeException("Seller chưa có shop"));

        // Cập nhật thông tin shop (chỉ cập nhật các trường không null)
        if (updateShopDTO.getName() != null && !updateShopDTO.getName().trim().isEmpty()) {
            shop.setName(updateShopDTO.getName().trim());
        }

        if (updateShopDTO.getAddress() != null && !updateShopDTO.getAddress().trim().isEmpty()) {
            shop.setAddress(updateShopDTO.getAddress().trim());
        }

        if (updateShopDTO.getDescription() != null && !updateShopDTO.getDescription().trim().isEmpty()) {
            shop.setDescription(updateShopDTO.getDescription().trim());
        }

        if (updateShopDTO.getStatus() != null && !updateShopDTO.getStatus().trim().isEmpty()) {
            // Validate status
            try {
                Shop.ShopStatus shopStatus = Shop.ShopStatus.valueOf(updateShopDTO.getStatus().toUpperCase());
                shop.setStatus(shopStatus);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Trạng thái không hợp lệ. Chỉ chấp nhận: ACTIVE hoặc INACTIVE");
            }
        }

        // Lưu vào database
        Shop updatedShop = shopRepository.save(shop);

        // Convert sang DTO và trả về
        return convertToDTO(updatedShop);
    }

    /**
     * Convert Shop entity sang ShopDTO
     */
    private ShopDTO convertToDTO(Shop shop) {
        ShopDTO dto = new ShopDTO();
        dto.setId(shop.getId());
        dto.setName(shop.getName());
        dto.setAddress(shop.getAddress());
        dto.setDescription(shop.getDescription());
        dto.setStatus(shop.getStatus() != null ? shop.getStatus().name() : null);
        dto.setCreatedAt(shop.getCreatedAt());
        return dto;
    }

    /**
     * Lấy thống kê thu nhập của shop
     * @param username - Username của seller từ JWT token
     * @param year - Năm cần thống kê (null = năm hiện tại)
     * @return ShopAnalyticsDTO - Thống kê thu nhập
     */
    public ShopAnalyticsDTO getShopAnalytics(String username, Integer year) {
        // Tìm user theo username
        User user = userRepository.findOneByUsername(username)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Kiểm tra role SELLER
        if (user.getRole() != com.PBL6.Ecommerce.domain.Role.SELLER) {
            throw new RuntimeException("Người dùng không phải là seller");
        }

        // Tìm shop của seller
        Shop shop = shopRepository.findByOwner(user)
            .orElseThrow(() -> new RuntimeException("Seller chưa có shop"));

        Long shopId = shop.getId();

        // Tính tổng doanh thu (chỉ đơn COMPLETED)
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue(shopId);

        // Đếm số đơn hàng COMPLETED
        Long totalCompletedOrders = orderRepository.countCompletedOrders(shopId);

        // Lấy doanh thu theo tháng
        List<MonthlyRevenueDTO> monthlyRevenue = new ArrayList<>();
        
        if (year == null) {
            year = LocalDateTime.now().getYear();
        }

        // Query doanh thu theo tháng của năm chỉ định
        List<Object[]> monthlyData = orderRepository.getMonthlyRevenueByYear(shopId, year);

        // Convert Object[] sang MonthlyRevenueDTO
        for (Object[] row : monthlyData) {
            int yearValue = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            BigDecimal revenue = (BigDecimal) row[2];
            Long orderCount = ((Number) row[3]).longValue();

            monthlyRevenue.add(new MonthlyRevenueDTO(yearValue, month, revenue, orderCount));
        }

        // Đảm bảo có đủ 12 tháng (fill 0 cho tháng không có doanh thu)
        List<MonthlyRevenueDTO> fullYearData = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            final int currentMonth = month;
            MonthlyRevenueDTO monthData = monthlyRevenue.stream()
                .filter(m -> m.getMonth() == currentMonth)
                .findFirst()
                .orElse(new MonthlyRevenueDTO(year, currentMonth, BigDecimal.ZERO, 0L));
            fullYearData.add(monthData);
        }

        // Tạo DTO kết quả
        return new ShopAnalyticsDTO(totalRevenue, totalCompletedOrders, fullYearData);
    }
}
