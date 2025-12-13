package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.*;
import com.PBL6.Ecommerce.domain.dto.CreateVoucherRequestDTO;
import com.PBL6.Ecommerce.domain.dto.TopBuyerDTO;
import com.PBL6.Ecommerce.domain.dto.VoucherApplicationResultDTO;
import com.PBL6.Ecommerce.domain.dto.VoucherDTO;
import com.PBL6.Ecommerce.domain.dto.VoucherPreviewDiscountDTO;
import com.PBL6.Ecommerce.domain.entity.product.Product;
import com.PBL6.Ecommerce.domain.entity.shop.Shop;
import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.domain.entity.voucher.VoucherProduct;
import com.PBL6.Ecommerce.domain.entity.voucher.VoucherUser;
import com.PBL6.Ecommerce.domain.entity.voucher.Vouchers;
import com.PBL6.Ecommerce.domain.entity.voucher.Vouchers.ApplicableType;
import com.PBL6.Ecommerce.domain.entity.voucher.Vouchers.DiscountType;
import com.PBL6.Ecommerce.domain.entity.voucher.Vouchers.Status;
import com.PBL6.Ecommerce.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class VoucherService {
    
    private static final Logger log = LoggerFactory.getLogger(VoucherService.class);
    
    private final VouchersRepository vouchersRepository;
    private final VoucherProductRepository voucherProductRepository;
    private final VoucherUserRepository voucherUserRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public VoucherService(VouchersRepository vouchersRepository,
                          VoucherProductRepository voucherProductRepository,
                          VoucherUserRepository voucherUserRepository,
                          ShopRepository shopRepository,
                          UserRepository userRepository,
                          ProductRepository productRepository,
                          OrderRepository orderRepository) {
        this.vouchersRepository = vouchersRepository;
        this.voucherProductRepository = voucherProductRepository;
        this.voucherUserRepository = voucherUserRepository;
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Tạo voucher mới cho shop
     */
    public VoucherDTO createVoucher(CreateVoucherRequestDTO request, Authentication authentication) {
        // Lấy seller từ authentication
        String username = authentication.getName();
        User seller = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy seller"));
        
        // Lấy shop của seller
        Shop shop = shopRepository.findByOwnerId(seller.getId())
            .orElseThrow(() -> new RuntimeException("Seller chưa có shop"));
        
        // Validate
        if (vouchersRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Mã voucher đã tồn tại");
        }
        
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("Ngày bắt đầu phải trước ngày kết thúc");
        }
        
        // Validate discount value
        if (Vouchers.DiscountType.PERCENTAGE.equals(request.getDiscountType())) {
            if (request.getDiscountValue().doubleValue() > 100) {
                throw new RuntimeException("Giá trị giảm giá phần trăm không được vượt quá 100%");
            }
        }
        
        // Tạo voucher
        Vouchers voucher = new Vouchers();
        voucher.setCode(request.getCode());
        voucher.setDescription(request.getDescription());
        voucher.setShop(shop);
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        voucher.setStartDate(request.getStartDate());
        voucher.setEndDate(request.getEndDate());
        voucher.setUsageLimit(request.getUsageLimit());
        voucher.setApplicableType(request.getApplicableType());
        voucher.setTopBuyersCount(request.getTopBuyersCount());
        
        Vouchers savedVoucher = vouchersRepository.save(voucher);
        
        // Lưu sản phẩm áp dụng (nếu có)
        if (com.PBL6.Ecommerce.domain.entity.voucher.Vouchers.ApplicableType.SPECIFIC_PRODUCTS.equals(request.getApplicableType()) && 
            request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            
            for (Long productId : request.getProductIds()) {
                Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + productId));
                
                // Kiểm tra product thuộc shop
                if (!product.getShop().getId().equals(shop.getId())) {
                    throw new RuntimeException("Sản phẩm ID " + productId + " không thuộc shop của bạn");
                }
                
                VoucherProduct vp = new VoucherProduct();
                vp.setVoucher(savedVoucher);
                vp.setProduct(product);
                voucherProductRepository.save(vp);
            }
        }
        
        // Lưu user áp dụng (nếu có)
        if (com.PBL6.Ecommerce.domain.entity.voucher.Vouchers.ApplicableType.SPECIFIC_USERS.equals(request.getApplicableType()) && 
            request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            
            for (Long userId : request.getUserIds()) {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user ID: " + userId));
                
                VoucherUser vu = new VoucherUser();
                vu.setVoucher(savedVoucher);
                vu.setUser(user);
                voucherUserRepository.save(vu);
            }
        }
        
        // Lưu top buyers (nếu có)
        if (com.PBL6.Ecommerce.domain.entity.voucher.Vouchers.ApplicableType.TOP_BUYERS.equals(request.getApplicableType()) && request.getTopBuyersCount() != null) {
            List<TopBuyerDTO> topBuyers = orderRepository.findTopBuyersByShopWithLimit(
                shop.getId(), 
                PageRequest.of(0, request.getTopBuyersCount())
            );
            
            for (TopBuyerDTO buyer : topBuyers) {
                User user = userRepository.findById(buyer.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user ID: " + buyer.getUserId()));
                
                VoucherUser vu = new VoucherUser();
                vu.setVoucher(savedVoucher);
                vu.setUser(user);
                voucherUserRepository.save(vu);
            }
        }
        
        return convertToDTO(savedVoucher);
    }

    /**
     * Lấy danh sách voucher của shop
     */
    @Transactional(readOnly = true)
    public List<VoucherDTO> getShopVouchers(Authentication authentication) {
        String username = authentication.getName();
        User seller = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy seller"));
        
        Shop shop = shopRepository.findByOwnerId(seller.getId())
            .orElseThrow(() -> new RuntimeException("Seller chưa có shop"));
        
        List<Vouchers> vouchers = vouchersRepository.findByShopId(shop.getId());
        return vouchers.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Lấy voucher đang active của shop
     */
    @Transactional(readOnly = true)
    public List<VoucherDTO> getActiveShopVouchers(Authentication authentication) {
        String username = authentication.getName();
        User seller = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy seller"));
        
        Shop shop = shopRepository.findByOwnerId(seller.getId())
            .orElseThrow(() -> new RuntimeException("Seller chưa có shop"));
        
        List<Vouchers> vouchers = vouchersRepository.findActiveVouchersByShop(shop.getId(), LocalDateTime.now());
        return vouchers.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Vô hiệu hóa voucher
     */
    public VoucherDTO deactivateVoucher(Long voucherId, Authentication authentication) {
        String username = authentication.getName();
        User seller = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy seller"));
        
        Shop shop = shopRepository.findByOwnerId(seller.getId())
            .orElseThrow(() -> new RuntimeException("Seller chưa có shop"));
        
        Vouchers voucher = vouchersRepository.findById(voucherId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));
        
        if (!voucher.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Bạn không có quyền vô hiệu hóa voucher này");
        }
        
        voucher.setStatus(Status.EXPIRED);
        Vouchers updated = vouchersRepository.save(voucher);
        
        return convertToDTO(updated);
    }

    /**
     * Convert Vouchers entity sang VoucherDTO
     */
    private VoucherDTO convertToDTO(Vouchers voucher) {
        VoucherDTO dto = new VoucherDTO();
        dto.setId(voucher.getId());
        dto.setCode(voucher.getCode());
        dto.setDescription(voucher.getDescription());
        dto.setShopId(voucher.getShop().getId());
        dto.setShopName(voucher.getShop().getName());
        dto.setDiscountType(voucher.getDiscountType());
        dto.setDiscountValue(voucher.getDiscountValue());
        dto.setMinOrderValue(voucher.getMinOrderValue());
        dto.setMaxDiscountAmount(voucher.getMaxDiscountAmount());
        dto.setStartDate(voucher.getStartDate());
        dto.setEndDate(voucher.getEndDate());
        dto.setUsageLimit(voucher.getUsageLimit());
        dto.setUsedCount(voucher.getUsedCount());
        dto.setApplicableType(voucher.getApplicableType());
        dto.setTopBuyersCount(voucher.getTopBuyersCount());
        dto.setStatus(voucher.getStatus());
        dto.setCreatedAt(voucher.getCreatedAt());
        
        // Lấy danh sách product IDs
        if (ApplicableType.SPECIFIC_PRODUCTS.equals(voucher.getApplicableType())) {
            dto.setProductIds(voucherProductRepository.findProductIdsByVoucherId(voucher.getId()));
        }
        
        // Lấy danh sách user IDs
        if (ApplicableType.SPECIFIC_USERS.equals(voucher.getApplicableType()) || 
            ApplicableType.TOP_BUYERS.equals(voucher.getApplicableType())) {
            dto.setUserIds(voucherUserRepository.findUserIdsByVoucherId(voucher.getId()));
        }
        
        return dto;
    }

    /**
     * Lấy danh sách voucher khả dụng cho user
     */
    @Transactional(readOnly = true)
    public List<VoucherDTO> getAvailableVouchersForUser(Long shopId, String username, List<Long> productIds, BigDecimal cartTotal) {
        // Get user
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Long userId = user.getId();
        
        // Verify shop exists
        shopRepository.findById(shopId)
            .orElseThrow(() -> new RuntimeException("Shop not found"));
        
        List<Vouchers> availableVouchers = new ArrayList<>();
        
        // Lấy voucher active của shop trong thời gian hiệu lực
        LocalDateTime now = LocalDateTime.now();
        List<Vouchers> activeVouchers = vouchersRepository.findActiveVouchersByShop(shopId, now);
        
        for (Vouchers voucher : activeVouchers) {
            // Kiểm tra usage limit
            if (voucher.getUsedCount() >= voucher.getUsageLimit()) {
                continue;
            }
            
            // Kiểm tra applicable type
            boolean isApplicable = checkVoucherApplicabilityForUser(voucher, userId, productIds);
            if (isApplicable) {
                availableVouchers.add(voucher);
            }
        }
        
        // Convert to DTO với preview discount
        return availableVouchers.stream()
            .map(voucher -> {
                VoucherDTO dto = convertToDTO(voucher);
                dto.setPreviewDiscount(calculatePreviewDiscount(voucher, cartTotal));
                return dto;
            })
            .collect(Collectors.toList());
    }

    private boolean checkVoucherApplicabilityForUser(Vouchers voucher, Long userId, List<Long> productIds) {
        switch (voucher.getApplicableType()) {
            case ALL:
                return true;
            case SPECIFIC_PRODUCTS:
                // Kiểm tra có sản phẩm nào trong cart thuộc voucher không
                return productIds.stream()
                    .anyMatch(productId -> voucherProductRepository.existsByVoucherIdAndProductId(voucher.getId(), productId));
            case SPECIFIC_USERS:
                // Kiểm tra user có trong danh sách áp dụng không
                return voucherUserRepository.existsByVoucherIdAndUserId(voucher.getId(), userId);
            case TOP_BUYERS:
                // Kiểm tra user có trong top buyers không
                return isUserInTopBuyers(userId, voucher.getTopBuyersCount());
            default:
                return false;
        }
    }

    private VoucherPreviewDiscountDTO calculatePreviewDiscount(Vouchers voucher, BigDecimal cartTotal) {
        if (voucher.getMinOrderValue() != null && cartTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            return new VoucherPreviewDiscountDTO(cartTotal, BigDecimal.ZERO, cartTotal);
        }
        
        BigDecimal discountAmount;
        if (DiscountType.PERCENTAGE.equals(voucher.getDiscountType())) {
            discountAmount = cartTotal.multiply(voucher.getDiscountValue().divide(BigDecimal.valueOf(100)));
            if (voucher.getMaxDiscountAmount() != null) {
                discountAmount = discountAmount.min(voucher.getMaxDiscountAmount());
            }
        } else {
            discountAmount = voucher.getDiscountValue();
        }
        
        BigDecimal finalTotal = cartTotal.subtract(discountAmount);
        return new VoucherPreviewDiscountDTO(cartTotal, discountAmount, finalTotal);
    }
    
    private boolean isUserInTopBuyers(Long userId, Integer topBuyersCount) {
        if (topBuyersCount == null || topBuyersCount <= 0) {
            return false;
        }
        
        List<TopBuyerDTO> topBuyers = orderRepository.findTopBuyers(
            PageRequest.of(0, topBuyersCount)
        ).getContent();
        
        return topBuyers.stream()
            .anyMatch(buyer -> buyer.getUserId().equals(userId));
    }

    /**
     * Áp dụng voucher cho đơn hàng
     */
    @Transactional
    public VoucherApplicationResultDTO applyVoucher(String voucherCode, List<Long> productIds, 
                                               BigDecimal cartTotal, String username) {
        log.info("Applying voucher: code={}, username={}, cartTotal={}", voucherCode, username, cartTotal);
        
        // Lấy user
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Tìm voucher
        Vouchers voucher = vouchersRepository.findByCode(voucherCode)
            .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));
        
        // Kiểm tra voucher còn active không
        if (!Status.ACTIVE.equals(voucher.getStatus())) {
            throw new RuntimeException("Voucher đã bị vô hiệu hóa");
        }
        
        // Kiểm tra thời gian hiệu lực
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getStartDate()) || now.isAfter(voucher.getEndDate())) {
            throw new RuntimeException("Voucher đã hết hạn hoặc chưa có hiệu lực");
        }
        
        // Kiểm tra số lần sử dụng
        if (voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new RuntimeException("Voucher đã hết lượt sử dụng");
        }
        
        // Kiểm tra điều kiện áp dụng
        if (!checkVoucherApplicabilityForUser(voucher, user.getId(), productIds)) {
            throw new RuntimeException("Bạn không đủ điều kiện sử dụng voucher này");
        }
        
        // Kiểm tra giá trị đơn hàng tối thiểu
        if (voucher.getMinOrderValue() != null && cartTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new RuntimeException("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng voucher");
        }
        
        // Tính toán giảm giá
        BigDecimal discountAmount = calculateDiscount(voucher, cartTotal);
        BigDecimal finalTotal = cartTotal.subtract(discountAmount);
        
        // Tăng số lần sử dụng voucher
        voucher.setUsedCount(voucher.getUsedCount() + 1);
        vouchersRepository.save(voucher);
        
        log.info("Voucher applied successfully: discountAmount={}, usedCount={}/{}", 
                 discountAmount, voucher.getUsedCount(), voucher.getUsageLimit());
        
        // Tạo kết quả
        VoucherApplicationResultDTO result = new VoucherApplicationResultDTO();
        result.setVoucher(convertToDTO(voucher));
        result.setOriginalTotal(cartTotal);
        result.setDiscountAmount(discountAmount);
        result.setFinalTotal(finalTotal);
        
        return result;
    }

    private BigDecimal calculateDiscount(Vouchers voucher, BigDecimal cartTotal) {
        if (voucher.getMinOrderValue() != null && cartTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discountAmount;
        if (DiscountType.PERCENTAGE.equals(voucher.getDiscountType())) {
            discountAmount = cartTotal.multiply(voucher.getDiscountValue().divide(BigDecimal.valueOf(100)));
            if (voucher.getMaxDiscountAmount() != null) {
                discountAmount = discountAmount.min(voucher.getMaxDiscountAmount());
            }
        } else {
            discountAmount = voucher.getDiscountValue();
        }
        
        return discountAmount;
    }
}
