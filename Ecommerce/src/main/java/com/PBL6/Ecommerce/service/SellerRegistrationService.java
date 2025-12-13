package com.PBL6.Ecommerce.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.constant.TypeAddress;
import com.PBL6.Ecommerce.domain.Address;
import com.PBL6.Ecommerce.domain.Role;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.Shop.ShopStatus;
import com.PBL6.Ecommerce.domain.dto.AdminApprovalDTO;
import com.PBL6.Ecommerce.domain.dto.AdminRejectionDTO;
import com.PBL6.Ecommerce.domain.dto.PendingApplicationDTO;
import com.PBL6.Ecommerce.domain.dto.RegistrationStatusDTO;
import com.PBL6.Ecommerce.domain.dto.SellerRegistrationRequestDTO;
import com.PBL6.Ecommerce.domain.dto.SellerRegistrationResponseDTO;
import com.PBL6.Ecommerce.repository.AddressRepository;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.PBL6.Ecommerce.repository.UserRepository;

/**
 * Service for handling Seller Registration workflow
 * Phase 1: COD only, Admin approval required
 */
@Service
public class SellerRegistrationService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final GhnService ghnService;

    @Value("${ghn.token}")
    private String ghnToken;

    public SellerRegistrationService(ShopRepository shopRepository, 
                                     UserRepository userRepository,
                                     AddressRepository addressRepository,
                                     GhnService ghnService) {
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.ghnService = ghnService;
    }

    /**
     * Submit seller registration application
     * Creates PENDING shop, requires admin approval
     * 
     * @param user - Current logged in BUYER
     * @param request - Registration data with KYC
     * @return SellerRegistrationResponseDTO
     */
    @Transactional
    public SellerRegistrationResponseDTO submitRegistration(User user, SellerRegistrationRequestDTO request) {
        // Validate user is BUYER
        if (user.getRole() != Role.BUYER) {
            throw new RuntimeException("Chỉ tài khoản BUYER mới có thể đăng ký bán hàng");
        }

        // Check if user already has a shop (any status)
        List<ShopStatus> activeOrPendingStatuses = Arrays.asList(ShopStatus.ACTIVE, ShopStatus.PENDING);
        if (shopRepository.findByOwnerAndStatusIn(user, activeOrPendingStatuses).isPresent()) {
            throw new RuntimeException("Bạn đã có đơn đăng ký đang chờ duyệt hoặc shop đang hoạt động");
        }

        // Check shop name uniqueness among ACTIVE and PENDING shops
        if (shopRepository.existsByNameAndStatusIn(request.getShopName().trim(), activeOrPendingStatuses)) {
            throw new RuntimeException("Tên shop đã tồn tại, vui lòng chọn tên khác");
        }

        // ========== CHECK DUPLICATE CCCD ==========
        // Prevent same ID card number from registering multiple shops
        if (request.getIdCardNumber() != null && !request.getIdCardNumber().trim().isEmpty()) {
            if (shopRepository.existsByIdCardNumberAndStatusIn(request.getIdCardNumber().trim(), activeOrPendingStatuses)) {
                throw new RuntimeException("Số CMND/CCCD này đã được sử dụng để đăng ký shop khác. Mỗi CCCD chỉ được đăng ký một shop.");
            }
        }

        // Create new shop with PENDING status
        Shop shop = new Shop();
        shop.setOwner(user);
        shop.setName(request.getShopName().trim());
        shop.setDescription(request.getDescription());
        shop.setStatus(ShopStatus.PENDING);
        shop.setCreatedAt(LocalDateTime.now());
        shop.setSubmittedAt(LocalDateTime.now());

        // Contact info
        shop.setShopPhone(request.getShopPhone());
        shop.setShopEmail(request.getShopEmail());

        // KYC info
        shop.setIdCardNumber(request.getIdCardNumber());
        shop.setIdCardFrontUrl(request.getIdCardFrontUrl());
        shop.setIdCardFrontPublicId(request.getIdCardFrontPublicId());
        shop.setIdCardBackUrl(request.getIdCardBackUrl());
        shop.setIdCardBackPublicId(request.getIdCardBackPublicId());
        shop.setSelfieWithIdUrl(request.getSelfieWithIdUrl());
        shop.setSelfieWithIdPublicId(request.getSelfieWithIdPublicId());
        shop.setIdCardName(request.getIdCardName());

        // Branding (optional)
        shop.setLogoUrl(request.getLogoUrl());
        shop.setLogoPublicId(request.getLogoPublicId());
        shop.setBannerUrl(request.getBannerUrl());
        shop.setBannerPublicId(request.getBannerPublicId());

        // Payment methods - Phase 1: COD only
        shop.setAcceptCod(true);
        shop.setCodFeePercentage(new BigDecimal("2.00")); // Default 2%

        // Initialize rating
        shop.setRating(new BigDecimal("5.00"));
        shop.setReviewCount(0);

        Shop savedShop = shopRepository.save(shop);

        // Handle address
        handleAddress(user, request, savedShop);

        return SellerRegistrationResponseDTO.success(
            "Đăng ký bán hàng thành công! Đơn của bạn đang chờ xét duyệt.",
            savedShop.getId(),
            savedShop.getName(),
            "PENDING"
        );
    }

    /**
     * Handle address creation or linking for shop registration
     */
    private void handleAddress(User user, SellerRegistrationRequestDTO request, Shop shop) {
        if (request.getAddressId() != null) {
            // Use existing address
            Address addr = addressRepository.findByIdAndUserId(request.getAddressId(), user.getId())
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại hoặc không thuộc user"));
            addr.setTypeAddress(TypeAddress.STORE);
            addressRepository.save(addr);
        } else if (request.getFullAddress() != null && !request.getFullAddress().trim().isEmpty()) {
            // Create new address
            Address addr = new Address();
            addr.setUser(user);
            addr.setFullAddress(request.getFullAddress().trim());
            addr.setProvinceId(request.getProvinceId());
            addr.setDistrictId(request.getDistrictId());
            addr.setWardCode(request.getWardCode());
            addr.setProvinceName(request.getProvinceName());
            addr.setDistrictName(request.getDistrictName());
            addr.setWardName(request.getWardName());
            addr.setContactPhone(request.getContactPhone() != null ? request.getContactPhone() : request.getShopPhone());
            addr.setContactName(request.getContactName() != null ? request.getContactName() : user.getFullName());
            addr.setPrimaryAddress(Boolean.TRUE.equals(request.getPrimaryAddress()));
            addr.setTypeAddress(TypeAddress.STORE);
            addressRepository.save(addr);
        }
    }

    /**
     * Get registration status for current user
     * 
     * @param user - Current logged in user
     * @return RegistrationStatusDTO with masked sensitive data
     */
    public RegistrationStatusDTO getRegistrationStatus(User user) {
        // Find shop for this user in any status
        Shop shop = shopRepository.findByOwner(user)
            .orElseThrow(() -> new RuntimeException("Bạn chưa có đơn đăng ký bán hàng"));

        return convertToStatusDTO(shop);
    }

    /**
     * Convert Shop to RegistrationStatusDTO (masks sensitive data)
     */
    private RegistrationStatusDTO convertToStatusDTO(Shop shop) {
        RegistrationStatusDTO dto = new RegistrationStatusDTO();
        dto.setShopId(shop.getId());
        dto.setShopName(shop.getName());
        dto.setDescription(shop.getDescription());
        dto.setShopPhone(shop.getShopPhone());
        dto.setShopEmail(shop.getShopEmail());
        dto.setStatus(shop.getStatus());
        dto.setStatusDescription(shop.getStatus());

        // Mask ID card number
        dto.setMaskedIdCardNumber(RegistrationStatusDTO.maskIdCardNumber(shop.getIdCardNumber()));
        dto.setIdCardName(shop.getIdCardName());

        // Get address
        Address storeAddress = addressRepository
            .findFirstByUserIdAndTypeAddress(shop.getOwner().getId(), TypeAddress.STORE)
            .orElse(null);
        if (storeAddress != null) {
            dto.setFullAddress(storeAddress.getFullAddress());
        }

        dto.setLogoUrl(shop.getLogoUrl());
        dto.setSubmittedAt(shop.getSubmittedAt());
        dto.setReviewedAt(shop.getReviewedAt());
        dto.setRejectionReason(shop.getRejectionReason());
        dto.setAcceptCod(shop.getAcceptCod());

        return dto;
    }

    /**
     * Get pending applications for admin
     * 
     * @param pageable - Pagination
     * @return Page of PendingApplicationDTO
     */
    public Page<PendingApplicationDTO> getPendingApplications(Pageable pageable) {
        Page<Shop> pendingShops = shopRepository.findByStatusOrderBySubmittedAtDesc(ShopStatus.PENDING, pageable);
        return pendingShops.map(this::convertToPendingDTO);
    }

    /**
     * Search pending applications by keyword
     * 
     * @param keyword - Search keyword (name, email, phone)
     * @param pageable - Pagination
     * @return Page of PendingApplicationDTO
     */
    public Page<PendingApplicationDTO> searchPendingApplications(String keyword, Pageable pageable) {
        Page<Shop> pendingShops = shopRepository.searchPendingApplications(keyword, pageable);
        return pendingShops.map(this::convertToPendingDTO);
    }

    /**
     * Convert Shop to PendingApplicationDTO (full KYC for admin)
     */
    private PendingApplicationDTO convertToPendingDTO(Shop shop) {
        PendingApplicationDTO dto = new PendingApplicationDTO();
        dto.setShopId(shop.getId());
        dto.setShopName(shop.getName());
        dto.setDescription(shop.getDescription());
        dto.setShopPhone(shop.getShopPhone());
        dto.setShopEmail(shop.getShopEmail());
        dto.setLogoUrl(shop.getLogoUrl());

        // Address
        Address storeAddress = addressRepository
            .findFirstByUserIdAndTypeAddress(shop.getOwner().getId(), TypeAddress.STORE)
            .orElse(null);
        if (storeAddress != null) {
            dto.setFullAddress(storeAddress.getFullAddress());
            dto.setProvinceName(storeAddress.getProvinceName());
            dto.setDistrictName(storeAddress.getDistrictName());
            dto.setWardName(storeAddress.getWardName());
        }

        // Owner info
        User owner = shop.getOwner();
        dto.setOwnerId(owner.getId());
        dto.setOwnerUsername(owner.getUsername());
        dto.setOwnerFullName(owner.getFullName());
        dto.setOwnerEmail(owner.getEmail());
        dto.setOwnerPhone(owner.getPhoneNumber());
        dto.setOwnerAvatar(owner.getAvatarUrl());
        dto.setOwnerCreatedAt(owner.getCreatedAt());

        // Full KYC for admin verification
        dto.setIdCardNumber(shop.getIdCardNumber());
        dto.setIdCardName(shop.getIdCardName());
        dto.setIdCardFrontUrl(shop.getIdCardFrontUrl());
        dto.setIdCardBackUrl(shop.getIdCardBackUrl());
        dto.setSelfieWithIdUrl(shop.getSelfieWithIdUrl());

        dto.setStatus(shop.getStatus());
        dto.setSubmittedAt(shop.getSubmittedAt());
        dto.setAcceptCod(shop.getAcceptCod());

        return dto;
    }

    /**
     * Get single pending application detail for admin
     * 
     * @param shopId - Shop ID
     * @return PendingApplicationDTO
     */
    public PendingApplicationDTO getApplicationDetail(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đăng ký"));
        
        return convertToPendingDTO(shop);
    }

    /**
     * Admin approves seller registration
     * - Changes shop status to ACTIVE
     * - Upgrades user role to SELLER
     * - Auto-register shop with GHN and save shop_id
     * 
     * @param admin - Admin user performing the action
     * @param approvalDTO - Approval request
     * @return SellerRegistrationResponseDTO
     */
    @Transactional
    public SellerRegistrationResponseDTO approveRegistration(User admin, AdminApprovalDTO approvalDTO) {
        // Validate admin role
        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Chỉ Admin mới có thể phê duyệt đơn đăng ký");
        }

        Shop shop = shopRepository.findById(approvalDTO.getShopId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đăng ký"));

        // Validate shop is PENDING
        if (shop.getStatus() != ShopStatus.PENDING) {
            throw new RuntimeException("Đơn đăng ký này không ở trạng thái chờ duyệt");
        }

        // ========== AUTO-REGISTER GHN SHOP ==========
        boolean ghnRegistrationSuccess = false;
        String ghnRegistrationError = null;
        
        try {
            // Get shop's store address
            Address storeAddress = addressRepository
                .findFirstByUserIdAndTypeAddress(shop.getOwner().getId(), TypeAddress.STORE)
                .orElse(null);

            System.out.println("========== GHN AUTO-REGISTRATION DEBUG ==========");
            System.out.println("Shop ID: " + shop.getId());
            System.out.println("Shop Name: " + shop.getName());
            System.out.println("Shop Phone: " + shop.getShopPhone());
            System.out.println("Store Address Found: " + (storeAddress != null));
            
            if (storeAddress != null) {
                System.out.println("Address Full: " + storeAddress.getFullAddress());
                System.out.println("District ID: " + storeAddress.getDistrictId());
                System.out.println("Ward Code: " + storeAddress.getWardCode());
            }
            System.out.println("================================================");

            if (storeAddress != null && storeAddress.getDistrictId() != null && storeAddress.getWardCode() != null) {
                System.out.println("🏪 Attempting to register shop with GHN...");
                
                // Call GHN API to register shop
                String ghnShopId = ghnService.registerGhnShop(
                    shop.getName(),
                    shop.getShopPhone(),
                    storeAddress
                );

                // Save only GHN shop ID (token is in application.properties)
                shop.setGhnShopId(ghnShopId);
                
                ghnRegistrationSuccess = true;
                System.out.println("✅ GHN shop registered successfully! Shop ID: " + ghnShopId);
            } else {
                ghnRegistrationError = "Missing address information (district_id or ward_code)";
                System.err.println("⚠️ Cannot register GHN shop: " + ghnRegistrationError);
                // Leave ghn_shop_id as null - will use application.properties token with no specific shop
                shop.setGhnShopId(null);
            }
        } catch (Exception e) {
            ghnRegistrationError = e.getMessage();
            System.err.println("❌ Failed to register GHN shop: " + ghnRegistrationError);
            e.printStackTrace();
            
            // Leave ghn_shop_id as null - will use application.properties token with no specific shop
            shop.setGhnShopId(null);
            
            System.out.println("⚠️ GHN registration failed - shop_id will be null");
        }

        // Update shop status
        shop.setStatus(ShopStatus.ACTIVE);
        shop.setReviewedAt(LocalDateTime.now());
        shop.setReviewedBy(admin.getId());
        shopRepository.save(shop);

        // Upgrade owner to SELLER role
        User owner = shop.getOwner();
        owner.setRole(Role.SELLER);
        userRepository.save(owner);

        // Build detailed success message
        String message;
        if (ghnRegistrationSuccess) {
            message = "Đã phê duyệt đơn đăng ký của " + shop.getName() + ". GHN shop đã được tự động tạo (Shop ID: " + shop.getGhnShopId() + ").";
        } else {
            message = "Đã phê duyệt đơn đăng ký của " + shop.getName() + ". " +
                     "⚠️ Không thể tự động đăng ký GHN shop" + 
                     (ghnRegistrationError != null ? " (Lỗi: " + ghnRegistrationError + ")" : "") + 
                     ". Seller cần đăng ký GHN shop thủ công.";
        }
        
        System.out.println("APPROVAL RESULT: " + message);

        return SellerRegistrationResponseDTO.success(
            message,
            shop.getId(),
            shop.getName(),
            "ACTIVE"
        );
    }

    /**
     * Admin rejects seller registration
     * - Changes shop status to REJECTED
     * - User remains BUYER
     * 
     * @param admin - Admin user performing the action
     * @param rejectionDTO - Rejection request with reason
     * @return SellerRegistrationResponseDTO
     */
    @Transactional
    public SellerRegistrationResponseDTO rejectRegistration(User admin, AdminRejectionDTO rejectionDTO) {
        // Validate admin role
        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Chỉ Admin mới có thể từ chối đơn đăng ký");
        }

        Shop shop = shopRepository.findById(rejectionDTO.getShopId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đăng ký"));

        // Validate shop is PENDING
        if (shop.getStatus() != ShopStatus.PENDING) {
            throw new RuntimeException("Đơn đăng ký này không ở trạng thái chờ duyệt");
        }

        // Update shop status
        shop.setStatus(ShopStatus.REJECTED);
        shop.setReviewedAt(LocalDateTime.now());
        shop.setReviewedBy(admin.getId());
        shop.setRejectionReason(rejectionDTO.getRejectionReason());
        shopRepository.save(shop);

        // Owner remains BUYER (no role change)

        return SellerRegistrationResponseDTO.success(
            "Đã từ chối đơn đăng ký của " + shop.getName(),
            shop.getId(),
            shop.getName(),
            "REJECTED"
        );
    }

    /**
     * Allow rejected user to re-submit registration
     * Deletes old rejected shop and allows new submission
     * 
     * @param user - User wanting to resubmit
     * @return boolean - true if old application was deleted
     */
    @Transactional
    public boolean cancelRejectedApplication(User user) {
        return shopRepository.findByOwnerAndStatus(user, ShopStatus.REJECTED)
            .map(shop -> {
                shopRepository.delete(shop);
                return true;
            })
            .orElse(false);
    }

    /**
     * Check if user can submit new registration
     */
    public boolean canSubmitRegistration(User user) {
        if (user.getRole() != Role.BUYER) {
            return false;
        }
        
        List<ShopStatus> blockingStatuses = Arrays.asList(ShopStatus.ACTIVE, ShopStatus.PENDING);
        return !shopRepository.findByOwnerAndStatusIn(user, blockingStatuses).isPresent();
    }

    /**
     * Get count of pending applications for admin dashboard
     */
    public long countPendingApplications() {
        return shopRepository.countByStatus(ShopStatus.PENDING);
    }
}
