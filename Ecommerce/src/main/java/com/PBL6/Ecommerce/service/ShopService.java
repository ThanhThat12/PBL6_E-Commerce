package com.PBL6.Ecommerce.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.MonthlyRevenueDTO;
import com.PBL6.Ecommerce.domain.dto.ShopAnalyticsDTO;
import com.PBL6.Ecommerce.domain.dto.ShopDTO;
import com.PBL6.Ecommerce.domain.dto.ShopRegistrationDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateShopDTO;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.PBL6.Ecommerce.repository.UserRepository;

@Service
public class ShopService {
    
    @Autowired
    private ShopRepository shopRepository;
    
    @Autowired
    private UserRepository userRepository;

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
    @Transactional
    public Shop registerShop(Long userId, ShopRegistrationDTO shopRegistrationDTO) {
        // Tìm user theo ID
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        // Kiểm tra xem user đã có shop chưa
        if (shopRepository.existsByOwner(user)) {
            throw new RuntimeException("Người dùng đã có shop");
        }
        
        // Kiểm tra tên shop đã tồn tại chưa
        if (shopRepository.existsByName(shopRegistrationDTO.getName())) {
            throw new RuntimeException("Tên shop đã tồn tại");
        }
        
        // Tạo shop mới
        Shop shop = new Shop();
        shop.setOwner(user);
        shop.setName(shopRegistrationDTO.getName());
        shop.setAddress(shopRegistrationDTO.getAddress());
        shop.setDescription(shopRegistrationDTO.getDescription());
        shop.setStatus(Shop.ShopStatus.ACTIVE);
        shop.setCreatedAt(LocalDateTime.now());
        
        return shopRepository.save(shop);
    }
    
    public Shop getShopByUserId(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        return shopRepository.findByOwner(user).orElse(null);
    }
    
    public boolean hasShop(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        return shopRepository.existsByOwner(user);
    }
    
    /**
     * Check if phone number already registered by another seller
     * Uses User entity to check phone uniqueness among SELLER role users
     * @param phone - Phone number to check
     * @return true if phone exists for another seller, false otherwise
     */
    public boolean existsByPhone(String phone) {
        // Check if any SELLER user has this phone number
        List<User> sellersWithPhone = userRepository.findByPhoneNumberAndRole(phone, com.PBL6.Ecommerce.domain.Role.SELLER);
        return !sellersWithPhone.isEmpty(); 
    }
    
    /**
     * Create shop from seller registration (Shopee-style upgrade)
     * @param user - User upgrading to seller
     * @param registrationDTO - Shop registration data
     * @return Created shop
     */
    @Transactional
    public Shop createShopFromSellerRegistration(User user, com.PBL6.Ecommerce.dto.seller.SellerRegistrationDTO registrationDTO) {
        // Validate user is BUYER
        if (user.getRole() != com.PBL6.Ecommerce.domain.Role.BUYER) {
            throw new RuntimeException("Chỉ BUYER mới có thể đăng ký seller");
        }
        
        // Validate no existing shop
        if (shopRepository.existsByOwner(user)) {
            throw new RuntimeException("User đã có shop");
        }
        
        // Validate phone uniqueness among sellers
        if (existsByPhone(registrationDTO.getShopPhone())) {
            throw new RuntimeException("Số điện thoại đã được sử dụng bởi seller khác");
        }
        
        // Validate shop name uniqueness
        if (shopRepository.existsByName(registrationDTO.getShopName())) {
            throw new RuntimeException("Tên shop đã tồn tại");
        }
        
        // Create shop
        Shop shop = new Shop();
        shop.setOwner(user);
        shop.setName(registrationDTO.getShopName());
        shop.setAddress(registrationDTO.getShopAddress());
        shop.setDescription(registrationDTO.getShopDescription());
        shop.setStatus(Shop.ShopStatus.ACTIVE);
        shop.setCreatedAt(LocalDateTime.now());
        
        Shop savedShop = shopRepository.save(shop);
        
        // Update user with fullName and phone (from registration)
        user.setFullName(registrationDTO.getFullName());
        user.setPhoneNumber(registrationDTO.getShopPhone());
        user.setRole(com.PBL6.Ecommerce.domain.Role.SELLER);
        userRepository.save(user);
        
        return savedShop;
    }
}
