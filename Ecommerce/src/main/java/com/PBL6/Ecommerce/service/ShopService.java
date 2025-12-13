package com.PBL6.Ecommerce.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.constant.TypeAddress;
import com.PBL6.Ecommerce.domain.Address;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.GhnCredentialsDTO;
import com.PBL6.Ecommerce.domain.dto.MonthlyRevenueDTO;
import com.PBL6.Ecommerce.domain.dto.ShopAnalyticsDTO;
import com.PBL6.Ecommerce.domain.dto.ShopDTO;
import com.PBL6.Ecommerce.domain.dto.ShopDetailDTO;
import com.PBL6.Ecommerce.domain.dto.ShopRegistrationDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateShopDTO;
import com.PBL6.Ecommerce.repository.AddressRepository;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.PBL6.Ecommerce.repository.UserRepository;

@Service
public class ShopService {
    
    @Autowired
    private ShopRepository shopRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

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

        return convertToDTO(shop);
    }

    /**
     * Cập nhật thông tin shop
     * @param username - Username của seller từ JWT token
     * @param updateShopDTO - Dữ liệu cập nhật
     * @return ShopDetailDTO - Thông tin shop đầy đủ sau khi cập nhật
     */
    @Transactional
    public ShopDetailDTO updateSellerShop(String username, UpdateShopDTO updateShopDTO) {
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

        // ========== Basic Info ==========
        if (updateShopDTO.getName() != null && !updateShopDTO.getName().trim().isEmpty()) {
            shop.setName(updateShopDTO.getName().trim());
        }

        if (updateShopDTO.getDescription() != null) {
            shop.setDescription(updateShopDTO.getDescription().trim());
        }

        // ========== Contact Info ==========
        if (updateShopDTO.getShopPhone() != null) {
            shop.setShopPhone(updateShopDTO.getShopPhone().trim());
        }
        if (updateShopDTO.getShopEmail() != null) {
            shop.setShopEmail(updateShopDTO.getShopEmail().trim());
        }

        // ========== Branding ==========
        if (updateShopDTO.getLogoUrl() != null) {
            shop.setLogoUrl(updateShopDTO.getLogoUrl());
            shop.setLogoPublicId(updateShopDTO.getLogoPublicId());
        }
        if (updateShopDTO.getBannerUrl() != null) {
            shop.setBannerUrl(updateShopDTO.getBannerUrl());
            shop.setBannerPublicId(updateShopDTO.getBannerPublicId());
        }

        // ========== GHN Credentials ==========
        if (updateShopDTO.getGhnShopId() != null && !updateShopDTO.getGhnShopId().trim().isEmpty()) {
            shop.setGhnShopId(updateShopDTO.getGhnShopId().trim());
        }
        if (updateShopDTO.getGhnToken() != null && !updateShopDTO.getGhnToken().trim().isEmpty()) {
            shop.setGhnToken(updateShopDTO.getGhnToken().trim());
        }

        // ========== Address Handling ==========
        if (updateShopDTO.getPickupAddressId() != null) {
            // Option 1: Use existing address ID
            Address addr = addressRepository.findByIdAndUserId(updateShopDTO.getPickupAddressId(), user.getId())
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại hoặc không thuộc user"));
            // Ensure type = STORE
            addr.setTypeAddress(TypeAddress.STORE);
            addressRepository.save(addr);
        } else if (updateShopDTO.getFullAddress() != null || updateShopDTO.getProvinceId() != null || 
                   updateShopDTO.getDistrictId() != null || updateShopDTO.getWardCode() != null) {
            // Option 2: Create or update store address with full details
            // Update if ANY address field is provided (not just fullAddress)
            Address storeAddr = addressRepository
                .findFirstByUserIdAndTypeAddress(user.getId(), TypeAddress.STORE)
                .orElse(null);
            
            if (storeAddr == null) {
                // Create new address
                storeAddr = new Address();
                storeAddr.setUser(user);
                storeAddr.setTypeAddress(TypeAddress.STORE);
            }
            
            // Update address fields - allow empty string to clear field
            if (updateShopDTO.getFullAddress() != null) {
                storeAddr.setFullAddress(updateShopDTO.getFullAddress().trim());
            }
            if (updateShopDTO.getProvinceId() != null) {
                storeAddr.setProvinceId(updateShopDTO.getProvinceId());
            }
            if (updateShopDTO.getDistrictId() != null) {
                storeAddr.setDistrictId(updateShopDTO.getDistrictId());
            }
            if (updateShopDTO.getWardCode() != null && !updateShopDTO.getWardCode().trim().isEmpty()) {
                storeAddr.setWardCode(updateShopDTO.getWardCode());
            }
            if (updateShopDTO.getProvinceName() != null) {
                storeAddr.setProvinceName(updateShopDTO.getProvinceName());
            }
            if (updateShopDTO.getDistrictName() != null) {
                storeAddr.setDistrictName(updateShopDTO.getDistrictName());
            }
            if (updateShopDTO.getWardName() != null) {
                storeAddr.setWardName(updateShopDTO.getWardName());
            }
            if (updateShopDTO.getContactPhone() != null) {
                storeAddr.setContactPhone(updateShopDTO.getContactPhone());
            }
            if (updateShopDTO.getContactName() != null) {
                storeAddr.setContactName(updateShopDTO.getContactName());
            }
            
            addressRepository.save(storeAddr);
        }

        // ========== Status (only allow ACTIVE/INACTIVE for seller self-update) ==========
        if (updateShopDTO.getStatus() != null && !updateShopDTO.getStatus().trim().isEmpty()) {
            try {
                Shop.ShopStatus shopStatus = Shop.ShopStatus.valueOf(updateShopDTO.getStatus().toUpperCase());
                // Seller can only set ACTIVE or INACTIVE
                if (shopStatus == Shop.ShopStatus.ACTIVE || shopStatus == Shop.ShopStatus.INACTIVE) {
                    shop.setStatus(shopStatus);
                } else {
                    throw new RuntimeException("Seller chỉ được phép đặt trạng thái ACTIVE hoặc INACTIVE");
                }
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Trạng thái không hợp lệ. Chỉ chấp nhận: ACTIVE hoặc INACTIVE");
            }
        }

        // Lưu vào database
        Shop updatedShop = shopRepository.save(shop);
        return convertToDetailDTO(updatedShop);
    }

    /**
     * Convert Shop entity sang ShopDTO (PUBLIC info only)
     * Used for: Guest viewing shop, Buyer viewing seller's shop, Product listing
     */
    private ShopDTO convertToDTO(Shop shop) {
        // Get store address
        Address storeAddress = null;
        if (shop.getOwner() != null) {
            storeAddress = addressRepository.findFirstByUserIdAndTypeAddress(shop.getOwner().getId(), TypeAddress.STORE)
                    .orElse(null);
        }

        return ShopDTO.builder()
            // Basic info
            .id(shop.getId())
            .name(shop.getName())
            .description(shop.getDescription())
            .status(shop.getStatus() != null ? shop.getStatus().name() : null)
            .createdAt(shop.getCreatedAt())
            
            // Branding
            .logoUrl(shop.getLogoUrl())
            .bannerUrl(shop.getBannerUrl())
            
            // Address (text only, no IDs)
            .address(storeAddress != null ? storeAddress.getFullAddress() : null)
            .provinceName(storeAddress != null ? storeAddress.getProvinceName() : null)
            .districtName(storeAddress != null ? storeAddress.getDistrictName() : null)
            .wardName(storeAddress != null ? storeAddress.getWardName() : null)
            
            // Rating
            .rating(shop.getRating())
            .reviewCount(shop.getReviewCount())
            
            // Contact (public)
            .shopPhone(shop.getShopPhone())
            .shopEmail(shop.getShopEmail())
            .build();
    }
    
    // Public helper to convert Shop -> ShopDTO for controllers
    public ShopDTO toDTO(Shop shop) {
        return convertToDTO(shop);
    }

    /**
     * Get full shop details for seller dashboard
     * Includes: address, GHN, KYC status, rating, owner info
     * 
     * @param username - Username of seller from JWT
     * @return ShopDetailDTO with all information
     */
    public ShopDetailDTO getSellerShopDetail(String username) {
        // Find user by username
        User user = userRepository.findOneByUsername(username)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Check SELLER role
        if (user.getRole() != com.PBL6.Ecommerce.domain.Role.SELLER) {
            throw new RuntimeException("Người dùng không phải là seller");
        }

        // Find seller's shop
        Shop shop = shopRepository.findByOwner(user)
            .orElseThrow(() -> new RuntimeException("Seller chưa có shop"));

        return convertToDetailDTO(shop);
    }

    /**
     * Convert Shop entity to ShopDetailDTO with full information
     */
    public ShopDetailDTO convertToDetailDTO(Shop shop) {
        User owner = shop.getOwner();
        
        // Get store address
        Address storeAddress = null;
        if (owner != null) {
            storeAddress = addressRepository.findFirstByUserIdAndTypeAddress(owner.getId(), TypeAddress.STORE)
                    .orElse(null);
        }

        ShopDetailDTO dto = ShopDetailDTO.builder()
            // Basic info
            .shopId(shop.getId())
            .name(shop.getName())
            .description(shop.getDescription())
            .status(shop.getStatus() != null ? shop.getStatus().name() : null)
            .createdAt(shop.getCreatedAt())
            
            // Contact
            .shopPhone(shop.getShopPhone())
            .shopEmail(shop.getShopEmail())
            
            // Branding
            .logoUrl(shop.getLogoUrl())
            .logoPublicId(shop.getLogoPublicId())
            .bannerUrl(shop.getBannerUrl())
            .bannerPublicId(shop.getBannerPublicId())
            
            // GHN (token managed in application.properties, only shop_id stored in DB)
            .ghnShopId(shop.getGhnShopId())
            .ghnToken(null) // Token không còn lưu trong DB
            .ghnConfigured(shop.getGhnShopId() != null && !shop.getGhnShopId().isEmpty())
            
            // KYC (masked for security)
            .maskedIdCardNumber(ShopDetailDTO.maskIdCardNumber(shop.getIdCardNumber()))
            .idCardName(shop.getIdCardName())
            .kycVerified(shop.getStatus() == Shop.ShopStatus.ACTIVE)
            
            // Payment
            .acceptCod(shop.getAcceptCod())
            .codFeePercentage(shop.getCodFeePercentage())
            
            // Rating
            .rating(shop.getRating())
            .reviewCount(shop.getReviewCount())
            
            // Review tracking
            .submittedAt(shop.getSubmittedAt())
            .reviewedAt(shop.getReviewedAt())
            .reviewedBy(shop.getReviewedBy())
            .rejectionReason(shop.getRejectionReason())
            .build();

        // Address info
        if (storeAddress != null) {
            dto.setAddressId(storeAddress.getId());
            dto.setFullAddress(storeAddress.getFullAddress());
            dto.setProvinceId(storeAddress.getProvinceId());
            dto.setDistrictId(storeAddress.getDistrictId());
            dto.setWardCode(storeAddress.getWardCode());
            dto.setProvinceName(storeAddress.getProvinceName());
            dto.setDistrictName(storeAddress.getDistrictName());
            dto.setWardName(storeAddress.getWardName());
            dto.setContactPhone(storeAddress.getContactPhone());
            dto.setContactName(storeAddress.getContactName());
        }

        // Owner info
        if (owner != null) {
            dto.setOwnerId(owner.getId());
            dto.setOwnerUsername(owner.getUsername());
            dto.setOwnerFullName(owner.getFullName());
            dto.setOwnerEmail(owner.getEmail());
            dto.setOwnerPhone(owner.getPhoneNumber());
            dto.setOwnerCreatedAt(owner.getCreatedAt());
        }

        // Show KYC images only for PENDING/REJECTED status (seller needs to review their submission)
        if (shop.getStatus() == Shop.ShopStatus.PENDING || shop.getStatus() == Shop.ShopStatus.REJECTED) {
            dto.setIdCardFrontUrl(shop.getIdCardFrontUrl());
            dto.setIdCardBackUrl(shop.getIdCardBackUrl());
            dto.setSelfieWithIdUrl(shop.getSelfieWithIdUrl());
        }

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
    // @Transactional
    // public Shop registerShop(Long userId, ShopRegistrationDTO shopRegistrationDTO) {
    //     // Tìm user theo ID
    //     User user = userRepository.findById(userId)
    //         .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
    //     // Kiểm tra xem user đã có shop chưa
    //     if (shopRepository.existsByOwner(user)) {
    //         throw new RuntimeException("Người dùng đã có shop");
    //     }
        
    //     // Kiểm tra tên shop đã tồn tại chưa
    //     if (shopRepository.existsByName(shopRegistrationDTO.getName())) {
    //         throw new RuntimeException("Tên shop đã tồn tại");
    //     }
        
    //     // Tạo shop mới
    //     Shop shop = new Shop();
    //     shop.setOwner(user);
    //     shop.setName(shopRegistrationDTO.getName());
    //     if (shopRegistrationDTO.getPickupAddressId() != null) {
    //         Address addr = addressRepository.findByIdAndUserId(shopRegistrationDTO.getPickupAddressId(), user.getId())
    //             .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại hoặc không thuộc user"));
    //         addr.setTypeAddress(TypeAddress.STORE);
    //         addressRepository.save(addr);
    //     } else if (shopRegistrationDTO.getAddress() != null && !shopRegistrationDTO.getAddress().trim().isEmpty()) {
    //         Address addr = new Address();
    //         addr.setUser(user);
    //         addr.setFullAddress(shopRegistrationDTO.getAddress().trim());
    //         addr.setTypeAddress(TypeAddress.STORE);
    //         Address savedAddr = addressRepository.save(addr);
    //     }
    //     shop.setDescription(shopRegistrationDTO.getDescription());
    //     shop.setStatus(Shop.ShopStatus.ACTIVE);
    //     shop.setCreatedAt(LocalDateTime.now());
        
    //     return shopRepository.save(shop);
    // }
    
    public Shop getShopByUserId(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        return shopRepository.findByOwner(user).orElse(null);
    }

    public Shop getShopByIdAndOwner(Long shopId, User user) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new RuntimeException("Shop không tồn tại"));
        if (shop.getOwner() == null || !shop.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền trên shop này");
        }
        return shop;
    }

    public Shop saveShop(Shop shop) {
        return shopRepository.save(shop);
    }

    public Shop getShopById(Long shopId) {
        return shopRepository.findById(shopId).orElseThrow(() -> new RuntimeException("Shop không tồn tại"));
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
    public Shop createShopFromSellerRegistration(User user, ShopRegistrationDTO registrationDTO) {
        // validate role / uniqueness omitted for brevity (keep existing checks)
        if (user.getRole() != com.PBL6.Ecommerce.domain.Role.BUYER) {
            throw new RuntimeException("Chỉ BUYER mới có thể đăng ký seller");
        }
        if (shopRepository.existsByOwner(user)) {
            throw new RuntimeException("User đã có shop");
        }
        if (registrationDTO.getName() == null || registrationDTO.getName().trim().isEmpty()) {
            throw new RuntimeException("Tên shop không được để trống");
        }
        if (shopRepository.existsByName(registrationDTO.getName())) {
            throw new RuntimeException("Tên shop đã tồn tại");
        }

        Shop shop = new Shop();
        shop.setOwner(user);
        shop.setName(registrationDTO.getName().trim());
        shop.setDescription(registrationDTO.getDescription());
        shop.setStatus(Shop.ShopStatus.ACTIVE);
        shop.setCreatedAt(LocalDateTime.now());
        Shop savedShop = shopRepository.save(shop);

        // address handling: chọn addressId (của chính user) hoặc tạo mới với đầy đủ trường
        if (registrationDTO.getAddressId() != null) {
            Address addr = addressRepository.findByIdAndUserId(registrationDTO.getAddressId(), user.getId())
                    .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại hoặc không thuộc user"));
            addr.setTypeAddress(TypeAddress.STORE);
            addressRepository.save(addr);
        } else if (registrationDTO.getFullAddress() != null && !registrationDTO.getFullAddress().trim().isEmpty()) {
            Address addr = new Address();
            addr.setUser(user);
            addr.setFullAddress(registrationDTO.getFullAddress().trim());
            addr.setProvinceId(registrationDTO.getProvinceId());
            addr.setDistrictId(registrationDTO.getDistrictId());
            addr.setWardCode(registrationDTO.getWardCode());
            addr.setProvinceName(registrationDTO.getProvinceName());
            addr.setDistrictName(registrationDTO.getDistrictName());
            addr.setWardName(registrationDTO.getWardName());
            addr.setContactPhone(registrationDTO.getContactPhone());
            addr.setContactName(registrationDTO.getContactName());
            addr.setPrimaryAddress(Boolean.TRUE.equals(registrationDTO.getPrimaryAddress()));
            addr.setTypeAddress(TypeAddress.STORE);
            addressRepository.save(addr);
        }

        // upgrade role to SELLER
        user.setRole(com.PBL6.Ecommerce.domain.Role.SELLER);
        userRepository.save(user);

        return savedShop;
    }

    /**
     * Cập nhật GHN credentials cho shop
     * @param shopId - ID của shop
     * @param userId - ID của user (để verify ownership)
     * @param dto - GHN credentials
     * @return Shop đã cập nhật
     */
    @Transactional
    public Shop updateGhnCredentials(Long shopId, Long userId, GhnCredentialsDTO dto) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new RuntimeException("Shop không tồn tại"));
        
        // Kiểm tra quyền sở hữu
        if (!shop.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền cập nhật shop này");
        }
        
        // Validate GHN token format nếu cần
        if (dto.getGhnToken() == null || dto.getGhnToken().trim().isEmpty()) {
            throw new RuntimeException("GHN Token không được để trống");
        }
        
        if (dto.getGhnShopId() == null || dto.getGhnShopId().trim().isEmpty()) {
            throw new RuntimeException("GHN Shop ID không được để trống");
        }
        
        shop.setGhnToken(dto.getGhnToken());
        shop.setGhnShopId(dto.getGhnShopId());
        
        return shopRepository.save(shop);
    }
}
