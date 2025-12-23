package com.PBL6.Ecommerce.service;

// ============================================
// DOMAIN IMPORTS
// ============================================
import com.PBL6.Ecommerce.domain.entity.user.Address;
import com.PBL6.Ecommerce.domain.entity.cart.Cart;
import com.PBL6.Ecommerce.domain.entity.order.Order;
import com.PBL6.Ecommerce.domain.entity.payment.Wallet;
import com.PBL6.Ecommerce.domain.entity.user.Role;
import com.PBL6.Ecommerce.domain.entity.product.Product;
import com.PBL6.Ecommerce.domain.entity.product.ProductReview;
import com.PBL6.Ecommerce.domain.entity.shop.Shop;
import com.PBL6.Ecommerce.domain.entity.shop.Shop.ShopStatus;
import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.domain.entity.auth.Verification;

// ============================================
// DTO IMPORTS
// ============================================
import com.PBL6.Ecommerce.domain.dto.ChangePasswordDTO;
import com.PBL6.Ecommerce.domain.dto.CheckContactDTO;
import com.PBL6.Ecommerce.domain.dto.RegisterDTO;
import com.PBL6.Ecommerce.domain.dto.TopBuyerDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateProfileDTO;
import com.PBL6.Ecommerce.domain.dto.UserInfoDTO;
import com.PBL6.Ecommerce.domain.dto.UserListDTO;
import com.PBL6.Ecommerce.domain.dto.UserProfileDTO;
import com.PBL6.Ecommerce.domain.dto.VerifyOtpDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminCreateAdminDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminChangePasswordDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminMyProfileDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminStatsDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminUpdateMyProfileDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminUpdateSellerDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminUpdateUserDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminUserDetailDTO;
import com.PBL6.Ecommerce.domain.dto.admin.CustomerStatsDTO;
import com.PBL6.Ecommerce.domain.dto.admin.ListAdminUserDTO;
import com.PBL6.Ecommerce.domain.dto.admin.ListCustomerUserDTO;
import com.PBL6.Ecommerce.domain.dto.admin.ListSellerUserDTO;
import com.PBL6.Ecommerce.domain.dto.admin.SellerStatsDTO;

// ============================================
// EXCEPTION IMPORTS
// ============================================
import com.PBL6.Ecommerce.exception.DuplicateEmailException;
import com.PBL6.Ecommerce.exception.DuplicateUsernameException;
import com.PBL6.Ecommerce.exception.DuplicatePhoneException;
import com.PBL6.Ecommerce.exception.ExpiredOtpException;
import com.PBL6.Ecommerce.exception.InvalidOtpException;
import com.PBL6.Ecommerce.exception.InvalidRoleException;
import com.PBL6.Ecommerce.exception.OtpNotVerifiedException;
import com.PBL6.Ecommerce.exception.PasswordMismatchException;
import com.PBL6.Ecommerce.exception.UnauthenticatedException;
import com.PBL6.Ecommerce.exception.UnauthorizedUserActionException;
import com.PBL6.Ecommerce.exception.UserHasReferencesException;
import com.PBL6.Ecommerce.exception.UserNotFoundException;

// ============================================
// REPOSITORY IMPORTS
// ============================================
import com.PBL6.Ecommerce.repository.AddressRepository;
import com.PBL6.Ecommerce.repository.CartItemRepository;
import com.PBL6.Ecommerce.repository.CartRepository;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.repository.ProductReviewRepository;
import com.PBL6.Ecommerce.repository.RefreshTokenRepository;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.repository.VerificationRepository;
import com.PBL6.Ecommerce.repository.VouchersRepository;
import com.PBL6.Ecommerce.repository.WalletRepository;

// ============================================
// SPRING IMPORTS
// ============================================
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageImpl;

// ============================================
// JAVA IMPORTS
// ============================================
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    // ============================================
    // DEPENDENCIES
    // ============================================
    private final UserRepository userRepository;
    private final VerificationRepository verificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SmsService smsService;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final VouchersRepository vouchersRepository;
    private final ProductReviewRepository productReviewRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final WalletRepository walletRepository;

    // ============================================
    // CONSTRUCTOR
    // ============================================
    public UserService(UserRepository userRepository,
                       VerificationRepository verificationRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService,
                       SmsService smsService,
                       CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ShopRepository shopRepository,
                       ProductRepository productRepository,
                       AddressRepository addressRepository,
                       OrderRepository orderRepository,
                       VouchersRepository vouchersRepository,
                       ProductReviewRepository productReviewRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.smsService = smsService;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.shopRepository = shopRepository;
        this.productRepository = productRepository;
        this.addressRepository = addressRepository;
        this.orderRepository = orderRepository;
        this.vouchersRepository = vouchersRepository;
        this.productReviewRepository = productReviewRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.walletRepository = walletRepository;
    }

    /**
     * Resolve current user from Authentication object
     * Supports ID, username, and email as principal
     */
    public User resolveCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new UserNotFoundException("Authentication is required");
        }
        
        String principal = authentication.getName();
        Optional<User> userOpt = Optional.empty();
        
        // Try to parse as ID first
        if (principal.matches("^\\d+$")) {
            try {
                Long id = Long.parseLong(principal);
                userOpt = userRepository.findById(id);
            } catch (NumberFormatException ignored) {
                // Not a valid ID, try other methods
            }
        }
        
        // Try username
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findOneByUsername(principal);
        }
        
        // Try email
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findOneByEmail(principal);
        }
        
        return userOpt.orElseThrow(() -> 
            new UserNotFoundException("User not found with principal: " + principal)
        );
    }

    /**
     * Extract user ID from JWT token subject
     */
    public Long extractUserIdFromJwt(org.springframework.security.oauth2.jwt.Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new UserNotFoundException("Invalid JWT token");
        }

        String subject = jwt.getSubject();
        // Nếu subject là số, trả về userId
        if (subject.matches("^\\d+$")) {
            try {
                return Long.parseLong(subject);
            } catch (NumberFormatException ignored) {
                // Không thể parse, tiếp tục xử lý bên dưới
            }
        }
        // Nếu subject là username, tìm user theo username
        Optional<User> userOpt = userRepository.findOneByUsername(subject);
        if (userOpt.isPresent()) {
            return userOpt.get().getId();
        }
        // Nếu không tìm thấy, thử tìm theo email
        userOpt = userRepository.findOneByEmail(subject);
        if (userOpt.isPresent()) {
            return userOpt.get().getId();
        }
        throw new UserNotFoundException("Invalid user ID or username in JWT token: " + subject);
    }

    public String checkContact(CheckContactDTO dto) {
        String contact = dto.getContact();
        
        log.debug("Checking contact availability: {}", contact);

        // 1. Kiểm tra tồn tại
        if (contact.contains("@")) {
            if (userRepository.existsByEmail(contact)) {
                log.warn("Email already exists: {}", contact);
                throw new DuplicateEmailException("Email already exists");
            }
        } else {
            if (userRepository.existsByPhoneNumber(contact)) {
                log.warn("Phone number already exists: {}", contact);
                throw new DuplicatePhoneException("Phone number already exists");
            }
        }

        // 2. Sinh OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        Verification verification = new Verification(
                contact,
                otp,
                LocalDateTime.now().plusMinutes(5),
                false,
                LocalDateTime.now()
        );
        verificationRepository.save(verification);

        // 3. Gửi OTP
        if (contact.contains("@")) {
            emailService.sendOtp(contact, otp);
            log.info("OTP sent to email: {}", contact);
        } else {
            smsService.sendOtp(contact, otp);
            log.info("OTP sent to phone: {}", contact);
        }

        return "OTP đã được gửi tới " + contact;
    }

    public String verifyOtp(VerifyOtpDTO dto) {
        log.debug("Verifying OTP for contact: {}", dto.getContact());
        
        Optional<Verification> verificationOpt =
                verificationRepository.findTopByContactOrderByCreatedAtDesc(dto.getContact());

        if (verificationOpt.isEmpty()) {
            log.warn("No OTP found for contact: {}", dto.getContact());
            throw new InvalidOtpException("OTP not found");
        }

        Verification verification = verificationOpt.get();
        if (!verification.getOtp().equals(dto.getOtp())) {
            log.warn("Invalid OTP for contact: {}", dto.getContact());
            throw new InvalidOtpException("Invalid OTP code");
        }
        if (verification.getExpiryTime().isBefore(LocalDateTime.now())) {
            log.warn("Expired OTP for contact: {}", dto.getContact());
            throw new ExpiredOtpException("OTP has expired");
        }

        verification.setVerified(true);
        verificationRepository.save(verification);

        log.info("OTP verified successfully for: {}", dto.getContact());
        return "Xác thực thành công";
    }

    public String register(RegisterDTO dto) {
        log.debug("Registering new user with username: {}", dto.getUsername());
        
        Verification verification = verificationRepository
                .findTopByContactOrderByCreatedAtDesc(dto.getContact())
                .orElseThrow(() -> new OtpNotVerifiedException("OTP verification required"));

        if (!verification.isVerified()) {
            log.warn("Registration failed - OTP not verified for: {}", dto.getContact());
            throw new OtpNotVerifiedException("OTP not verified");
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            log.warn("Registration failed - password mismatch for: {}", dto.getUsername());
            throw new PasswordMismatchException("Password and confirm password do not match");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        if (dto.getContact().contains("@")) {
            user.setEmail(dto.getContact());
        } else {
            user.setPhoneNumber(dto.getContact());
        }

        user.setActivated(true);
        user.setRole(Role.BUYER);

        userRepository.save(user);

        log.info("User registered successfully: {}", dto.getUsername());
        return "Đăng ký thành công";
    }
    // cập nhật mật khẩu cho email hoặc phone
    public void updatePassword(String contact, String newPassword) {
        User user = null;

        if (contact.contains("@")) { // nếu là email
            user = userRepository.findOneByEmail(contact)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user với email: " + contact));
        } else { // nếu là số điện thoại
            user = userRepository.findOneByPhoneNumber(contact)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user với số điện thoại: " + contact));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public UserInfoDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthenticated access attempt");
            throw new UnauthenticatedException("Authentication required");
        }

        String principal = authentication.getName();
        Optional<User> userOpt = Optional.empty();

        // Nếu principal là numeric -> tìm theo id (TokenProvider đặt sub = userId)
        if (principal != null && principal.matches("^\\d+$")) {
            try {
                Long id = Long.parseLong(principal);
                userOpt = userRepository.findById(id);
            } catch (NumberFormatException ignored) { }
        }

        // Nếu chưa tìm theo id thì thử username -> email
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findOneByUsername(principal);
        }
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findOneByEmail(principal);
        }

        User user = userOpt.orElseThrow(() -> {
            log.warn("User not found for principal: {}", principal);
            return new UserNotFoundException("User not found");
        });

        return new UserInfoDTO(user.getId(), user.getEmail(), user.getUsername(), user.getRole().name(), user.getFullName());
    }

    /**
     * Get full user profile with all fields including metadata
     */
    public UserProfileDTO getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthenticated access attempt");
            throw new UnauthenticatedException("Authentication required");
        }

        User user = resolveCurrentUser(authentication);
        
        return new UserProfileDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getFullName(),
            user.getRole().name(),
            user.getAvatarUrl(),
            user.isActivated(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    /**
     * Update user profile information
     */
    @Transactional
    public UserProfileDTO updateProfile(UpdateProfileDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthenticatedException("Authentication required");
        }

        User user = resolveCurrentUser(authentication);

        // Check if username is being changed and already exists
        if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
            if (userRepository.findOneByUsername(dto.getUsername()).isPresent()) {
                throw new RuntimeException("Username already exists");
            }
            user.setUsername(dto.getUsername());
        }

        // Check if email is being changed and already exists
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            if (userRepository.findOneByEmail(dto.getEmail()).isPresent()) {
                throw new DuplicateEmailException("Email already exists");
            }
            user.setEmail(dto.getEmail());
        }

        // Check if phone is being changed and already exists
        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.findOneByPhoneNumber(dto.getPhoneNumber()).isPresent()) {
                throw new DuplicatePhoneException("Phone number already exists");
            }
            user.setPhoneNumber(dto.getPhoneNumber());
        }

        // Update fullName if provided
        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }

        User updatedUser = userRepository.save(user);
        log.info("Profile updated for user: {}", updatedUser.getUsername());

        return new UserProfileDTO(
            updatedUser.getId(),
            updatedUser.getUsername(),
            updatedUser.getEmail(),
            updatedUser.getPhoneNumber(),
            updatedUser.getFullName(),
            updatedUser.getRole().name(),
            updatedUser.getAvatarUrl(),
            updatedUser.isActivated(),
            updatedUser.getCreatedAt(),
            updatedUser.getUpdatedAt()
        );
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(ChangePasswordDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthenticatedException("Authentication required");
        }

        User user = resolveCurrentUser(authentication);

        // Verify old password
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new PasswordMismatchException("Old password is incorrect");
        }

        // Verify new password matches confirmation
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new PasswordMismatchException("New password and confirmation do not match");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getUsername());
    }

    /**
     * Update user avatar URL
     */
    @Transactional
    public UserProfileDTO updateAvatar(String avatarUrl) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthenticatedException("Authentication required");
        }

        User user = resolveCurrentUser(authentication);
        user.setAvatarUrl(avatarUrl);
        User updatedUser = userRepository.save(user);
        log.info("Avatar updated for user: {}", updatedUser.getUsername());

        return new UserProfileDTO(
            updatedUser.getId(),
            updatedUser.getUsername(),
            updatedUser.getEmail(),
            updatedUser.getPhoneNumber(),
            updatedUser.getFullName(),
            updatedUser.getRole().name(),
            updatedUser.getAvatarUrl(),
            updatedUser.isActivated(),
            updatedUser.getCreatedAt(),
            updatedUser.getUpdatedAt()
        );
    }

    public List<UserListDTO> getAllUsers() {
        List<User> users = userRepository.getAllUsers();
        return users.stream()
                .map(user -> new UserListDTO(
                    user.getId(),
                    user.isActivated(),
                    user.getEmail(),
                    user.getUsername()
                ))
                .collect(Collectors.toList());
    }

    public List<UserInfoDTO> getUsersByRole(String roleName) {
        log.debug("Getting users by role: {}", roleName);
        
        Role role;
        try {
            role = Role.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role requested: {}", roleName);
            throw new InvalidRoleException("Invalid role: " + roleName);
        }
        
        List<User> users = userRepository.findByRole(role);
        return users.stream()
                .map(user -> new UserInfoDTO(
                    user.getId(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getRole().name()
                ))
                .collect(Collectors.toList());
    }

    
    /**
     * Xóa verification codes theo email và phone
     */
    private void deleteUserVerifications(User user) {
        // Xóa verification theo email
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            verificationRepository.deleteByContact(user.getEmail());
        }
        
        // Xóa verification theo phone number
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            verificationRepository.deleteByContact(user.getPhoneNumber());
        }
    }

    // ================================
    // TOP BUYERS METHODS
    // ================================

    /**
     * Lấy danh sách tất cả top buyers (cho ADMIN)
     */
    @Transactional(readOnly = true)
    public List<TopBuyerDTO> getAllTopBuyers() {
        return orderRepository.findTopBuyers();
    }

    /**
     * Lấy danh sách top buyers với phân trang (cho ADMIN)
     */
    @Transactional(readOnly = true)
    public Page<TopBuyerDTO> getAllTopBuyers(Pageable pageable) {
        return orderRepository.findTopBuyers(pageable);
    }

    /**
     * Lấy top N buyers (cho ADMIN)
     */
    @Transactional(readOnly = true)
    public List<TopBuyerDTO> getTopBuyers(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return orderRepository.findTopBuyersWithLimit(pageable);
    }

    /**
     * Lấy top buyers theo shop cụ thể (cho SELLER)
     */
    @Transactional(readOnly = true)
    public List<TopBuyerDTO> getTopBuyersByShop(String username) {
        // Tìm user theo username
        User seller = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy seller"));
        
        // Tìm shop của seller
        Shop shop = shopRepository.findByOwnerId(seller.getId())
                .orElseThrow(() -> new RuntimeException("Seller chưa có shop"));
        
        return orderRepository.findTopBuyersByShop(shop.getId());
    }

    /**
     * Lấy top buyers theo shop ID cụ thể (cho ADMIN)
     */
    @Transactional(readOnly = true)
    public List<TopBuyerDTO> getTopBuyersByShopId(Long shopId) {
        // Kiểm tra shop có tồn tại không
        shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy shop với ID: " + shopId));
        
        return orderRepository.findTopBuyersByShop(shopId);
    }

    /**
     * Lấy top buyers theo shop ID với giới hạn số lượng (cho ADMIN)
     */
    @Transactional(readOnly = true)
    public List<TopBuyerDTO> getTopBuyersByShopIdWithLimit(Long shopId, int limit) {
        // Kiểm tra shop có tồn tại không
        shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy shop với ID: " + shopId));
        
        Pageable pageable = PageRequest.of(0, limit);
        return orderRepository.findTopBuyersByShopWithLimit(shopId, pageable);
    }

    /**
     * Lấy top buyers của shop với giới hạn số lượng (cho SELLER)
     */
    @Transactional(readOnly = true)
    public List<TopBuyerDTO> getTopBuyersByShopWithLimit(String username, int limit) {
        // Tìm user theo username
        User seller = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy seller"));
        
        // Tìm shop của seller
        Shop shop = shopRepository.findByOwnerId(seller.getId())
                .orElseThrow(() -> new RuntimeException("Seller chưa có shop"));
        
        Pageable pageable = PageRequest.of(0, limit);
        return orderRepository.findTopBuyersByShopWithLimit(shop.getId(), pageable);
    }



































    
    
    // -------------------ADMIN-------------------
    // ***Hiển thị từng trang User theo role*** 
    public List<ListAdminUserDTO> getAdminUsers() {
        List<User> users = userRepository.findByRole(Role.ADMIN);
        return users.stream()
                .map(user -> new ListAdminUserDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getRole().name(),
                    user.isActivated(),
                    user.getCreatedAt(),
                    null  // lastLogin - User entity không có field này
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy danh sách admin users với phân trang
     * @param pageable - Thông tin phân trang (page, size, sort)
     * @return Page<ListAdminUserDTO>
     */
    @Transactional(readOnly = true)
    public Page<ListAdminUserDTO> getAdminUsers(Pageable pageable) {
        log.debug("Getting admin users with pagination - page: {}, size: {}", 
            pageable.getPageNumber(), pageable.getPageSize());
            
        Page<User> userPage = userRepository.findByRole(Role.ADMIN, pageable);
        
        return userPage.map(user -> new ListAdminUserDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getRole().name(),
            user.isActivated(),
            user.getCreatedAt(),
            null  // lastLogin - User entity không có field này
        ));
    }

    public List<ListSellerUserDTO> getSellerUsers() {
        List<User> users = userRepository.findByRole(Role.SELLER);
        return users.stream()
                .map(user -> {
                    // Lấy shop của seller
                    Optional<Shop> shopOpt = shopRepository.findByOwnerId(user.getId());
                    
                    String shopName = null;
                    String status = null;
                    int totalProducts = 0;
                    double revenue = 0.0;
                    String logoUrl = null;
                    
                    if (shopOpt.isPresent()) {
                        Shop shop = shopOpt.get();
                        shopName = shop.getName();
                        status = shop.getStatus() != null ? shop.getStatus().name() : "PENDING";
                        logoUrl = shop.getLogoUrl();
                        
                        // Đếm tổng sản phẩm của shop
                        totalProducts = (int) productRepository.countByShopId(shop.getId());
                        
                        // Tính tổng doanh thu từ các đơn hàng COMPLETED
                        Double rev = orderRepository.getTotalRevenueByShopId(shop.getId());
                        revenue = rev != null ? rev : 0.0;
                    }
                    
                    return new ListSellerUserDTO(
                        user.getId(),
                        shopName,
                        shopOpt.map(Shop::getShopPhone).orElse(null),
                        user.getEmail(),
                        totalProducts,
                        status,
                        revenue,
                        logoUrl
                    );
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy danh sách SELLER với phân trang
     * @param pageable - Thông tin phân trang (page, size, sort)
     * @return Page<ListSellerUserDTO>
     */
    @Transactional(readOnly = true)
    public Page<ListSellerUserDTO> getSellerUsers(Pageable pageable) {
        log.debug("Getting seller users with pagination - page: {}, size: {}", 
            pageable.getPageNumber(), pageable.getPageSize());
            
        Page<User> userPage = userRepository.findByRole(Role.SELLER, pageable);
        
        return userPage.map(user -> {
            // Lấy shop của seller
            Optional<Shop> shopOpt = shopRepository.findByOwnerId(user.getId());
            
            String shopName = null;
            String status = null;
            int totalProducts = 0;
            double revenue = 0.0;
            String logoUrl = null;
            
            if (shopOpt.isPresent()) {
                Shop shop = shopOpt.get();
                shopName = shop.getName();
                status = shop.getStatus() != null ? shop.getStatus().name() : "PENDING";
                logoUrl = shop.getLogoUrl();
                
                // Đếm tổng sản phẩm của shop
                totalProducts = (int) productRepository.countByShopId(shop.getId());
                
                // Tính tổng doanh thu từ các đơn hàng COMPLETED
                Double rev = orderRepository.getTotalRevenueByShopId(shop.getId());
                revenue = rev != null ? rev : 0.0;
            }
            
            return new ListSellerUserDTO(
                user.getId(),
                shopName,
                shopOpt.map(Shop::getShopPhone).orElse(null),
                user.getEmail(),
                totalProducts,
                status,
                revenue,
                logoUrl
            );
        });
    }

    public List<ListCustomerUserDTO> getCustomerUsers() {
        List<User> users = userRepository.findByRole(Role.BUYER);
        return users.stream()
                .map(user -> {
                    // ✅ Lấy thông tin đơn hàng thực tế
                    long totalOrders = orderRepository.countCompletedOrdersByUserId(user.getId());
                    Double totalSpent = orderRepository.getTotalSpentByUserId(user.getId());
                    LocalDateTime lastOrderDate = orderRepository.getLastCompletedOrderDateByUserId(user.getId()).orElse(null);
                    
                    return new ListCustomerUserDTO(
                        user.getId(),
                        user.getFullName(),          // ✅ Thêm fullName
                        user.getUsername(),          // ✅ Đã có username
                        user.getEmail(),
                        user.getPhoneNumber(),       // ✅ Đã có phoneNumber
                        user.isActivated(),          // ✅ Đã có activated status
                        user.getCreatedAt(),         // registeredAt
                        lastOrderDate,               // ✅ lastOrderDate thật
                        (int) totalOrders,           // ✅ totalOrders thật
                        totalSpent != null ? totalSpent : 0.0  // ✅ totalSpent thật
                    );
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy danh sách customer users với phân trang
     * @param pageable - Thông tin phân trang (page, size, sort)
     * @return Page<ListCustomerUserDTO>
     */
    @Transactional(readOnly = true)
    public Page<ListCustomerUserDTO> getCustomerUsers(Pageable pageable) {
        log.debug("Getting customer users with pagination - page: {}, size: {}", 
            pageable.getPageNumber(), pageable.getPageSize());
        
        // Sort by ID descending (newest first)
        Pageable sortedPageable = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id")
        );
            
        Page<User> userPage = userRepository.findByRole(Role.BUYER, sortedPageable);
        
        return userPage.map(user -> {
            // ✅ Lấy thông tin đơn hàng thực tế
            long totalOrders = orderRepository.countCompletedOrdersByUserId(user.getId());
            Double totalSpent = orderRepository.getTotalSpentByUserId(user.getId());
            LocalDateTime lastOrderDate = orderRepository.getLastCompletedOrderDateByUserId(user.getId()).orElse(null);
            
            return new ListCustomerUserDTO(
                user.getId(),
                user.getFullName(),          // ✅ Thêm fullName
                user.getUsername(),          // ✅ Đã có username
                user.getEmail(),
                user.getPhoneNumber(),       // ✅ Đã có phoneNumber
                user.isActivated(),          // ✅ Đã có activated status
                user.getCreatedAt(),         // registeredAt
                lastOrderDate,               // ✅ lastOrderDate thật
                (int) totalOrders,           // ✅ totalOrders thật
                totalSpent != null ? totalSpent : 0.0  // ✅ totalSpent thật
            );
        });
    }

    /**
     * Get customer statistics for admin dashboard
     * Returns: Total customers, active customers, new this month, total revenue
     */
    @Transactional(readOnly = true)
    public CustomerStatsDTO getCustomerStats() {
        log.debug("Getting customer statistics");
        
        // 1. Tổng số customers (role = BUYER)
        long totalCustomers = userRepository.countByRole(Role.BUYER);
        
        // 2. Active customers (activated = true AND role = BUYER)
        long activeCustomers = userRepository.countByRoleAndActivated(Role.BUYER, true);
        
        // 3. New customers this month
        LocalDateTime startOfMonth = LocalDateTime.now()
            .withDayOfMonth(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        long newThisMonth = userRepository.countByRoleAndCreatedAtAfter(Role.BUYER, startOfMonth);
        
        // 4. Total revenue from all completed orders
        Double totalRevenue = orderRepository.getTotalRevenueFromCompletedOrders();
        
        log.info("Customer stats - Total: {}, Active: {}, New: {}, Revenue: {}", 
            totalCustomers, activeCustomers, newThisMonth, totalRevenue);
        
        return new CustomerStatsDTO(
            totalCustomers,
            activeCustomers,    
            newThisMonth,
            totalRevenue != null ? totalRevenue : 0.0
        );
    }

    /**
     * Get admin statistics for admin dashboard
     * Returns: Total admins, active admins, inactive admins
     */
    @Transactional(readOnly = true)
    public AdminStatsDTO getAdminStats() {
        log.debug("Getting admin statistics");
        
        // 1. Tổng số admins (role = ADMIN)
        long totalAdmins = userRepository.countByRole(Role.ADMIN);
        
        // 2. Active admins (activated = true AND role = ADMIN)
        long activeAdmins = userRepository.countByRoleAndActivated(Role.ADMIN, true);
        
        // 3. Inactive admins (activated = false AND role = ADMIN)
        long inactiveAdmins = userRepository.countByRoleAndActivated(Role.ADMIN, false);
        
        log.info("Admin stats - Total: {}, Active: {}, Inactive: {}", 
            totalAdmins, activeAdmins, inactiveAdmins);
        
        return new AdminStatsDTO(
            totalAdmins,
            activeAdmins,
            inactiveAdmins
        );
    }

    /**
     * Get seller statistics for admin dashboard
     * Returns: Total sellers, active sellers, pending sellers, inactive sellers
     */
    @Transactional(readOnly = true)
    public SellerStatsDTO getSellerStats() {
        log.debug("Getting seller statistics");
        
        // 1. Tổng số sellers (role = SELLER)
        long totalSellers = userRepository.countByRole(Role.SELLER);
        
        // 2. Active sellers (shop status = ACTIVE)
        long activeSellers = shopRepository.countByStatus(ShopStatus.ACTIVE);
        
        // 3. Pending sellers (shop status = PENDING)
        long pendingSellers = shopRepository.countByStatus(ShopStatus.PENDING);
        
        // 4. Inactive sellers (shop status = INACTIVE)
        long inactiveSellers = shopRepository.countByStatus(ShopStatus.INACTIVE);
        
        log.info("Seller stats - Total: {}, Active: {}, Pending: {}, Inactive: {}", 
            totalSellers, activeSellers, pendingSellers, inactiveSellers);
        
        return new SellerStatsDTO(
            totalSellers,
            activeSellers,
            pendingSellers,
            inactiveSellers
        );
    }

    /**
     * Get sellers filtered by status
     * @param status - ACTIVE, PENDING, INACTIVE, or null for all
     * @return List of sellers matching the status
     */
    @Transactional(readOnly = true)
    public List<ListSellerUserDTO> getSellersByStatus(String status) {
        log.debug("Getting sellers by status: {}", status);
        
        List<User> users = userRepository.findByRole(Role.SELLER);
        
        return users.stream()
                .map(user -> {
                    // Lấy shop của seller
                    Optional<Shop> shopOpt = shopRepository.findByOwnerId(user.getId());
                    
                    String shopName = null;
                    String shopStatus = null;
                    int totalProducts = 0;
                    double revenue = 0.0;
                    String logoUrl = null;
                    
                    if (shopOpt.isPresent()) {
                        Shop shop = shopOpt.get();
                        shopName = shop.getName();
                        shopStatus = shop.getStatus() != null ? shop.getStatus().name() : "PENDING";
                        logoUrl = shop.getLogoUrl();
                        
                        // Đếm tổng sản phẩm của shop
                        totalProducts = (int) productRepository.countByShopId(shop.getId());
                        
                        // Tính tổng doanh thu từ các đơn hàng COMPLETED
                        Double rev = orderRepository.getTotalRevenueByShopId(shop.getId());
                        revenue = rev != null ? rev : 0.0;
                    }
                    
                    return new ListSellerUserDTO(
                        user.getId(),
                        shopName,
                        shopOpt.map(Shop::getShopPhone).orElse(null),
                        user.getEmail(),
                        totalProducts,
                        shopStatus,
                        revenue,
                        logoUrl
                    );
                })
                .filter(seller -> {
                    if (status == null || status.isEmpty()) {
                        return true;
                    }
                    return status.equalsIgnoreCase(seller.getStatus());
                })
                .collect(Collectors.toList());
    }

    /**
     * Get sellers filtered by status with pagination
     * @param status - ACTIVE, PENDING, INACTIVE, or null for all
     * @param pageable - Pagination information (page, size, sort)
     * @return Page of sellers matching the status
     */
    @Transactional(readOnly = true)
    public Page<ListSellerUserDTO> getSellersByStatusWithPaging(String status, Pageable pageable) {
        log.debug("Getting sellers by status '{}' with pagination: page={}, size={}", 
            status, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<User> userPage = userRepository.findByRole(Role.SELLER, pageable);
        
        List<ListSellerUserDTO> sellerDTOs = userPage.getContent().stream()
                .map(user -> {
                    // Lấy shop của seller
                    Optional<Shop> shopOpt = shopRepository.findByOwnerId(user.getId());
                    
                    String shopName = null;
                    String shopStatus = null;
                    int totalProducts = 0;
                    double revenue = 0.0;
                    String logoUrl = null;
                    
                    if (shopOpt.isPresent()) {
                        Shop shop = shopOpt.get();
                        shopName = shop.getName();
                        shopStatus = shop.getStatus() != null ? shop.getStatus().name() : "PENDING";
                        logoUrl = shop.getLogoUrl();
                        
                        // Đếm tổng sản phẩm của shop
                        totalProducts = (int) productRepository.countByShopId(shop.getId());
                        
                        // Tính tổng doanh thu từ các đơn hàng COMPLETED
                        Double rev = orderRepository.getTotalRevenueByShopId(shop.getId());
                        revenue = rev != null ? rev : 0.0;
                    }
                    
                    return new ListSellerUserDTO(
                        user.getId(),
                        shopName,
                        shopOpt.map(Shop::getShopPhone).orElse(null),
                        user.getEmail(),
                        totalProducts,
                        shopStatus,
                        revenue,
                        logoUrl
                    );
                })
                .filter(seller -> {
                    // Lọc theo status nếu có
                    if (status == null || status.isEmpty() || "ALL".equalsIgnoreCase(status)) {
                        return true;
                    }
                    return status.equalsIgnoreCase(seller.getStatus());
                })
                .collect(Collectors.toList());
        
        // Tạo Page mới với kết quả đã lọc
        // Lưu ý: Việc lọc sau khi query có thể làm thay đổi số lượng thực tế
        // Nếu cần chính xác tuyệt đối, nên dùng custom query với JOIN
        return new PageImpl<>(sellerDTOs, pageable, userPage.getTotalElements());
    }


    /**
     * Create a new admin account
     * @param dto - AdminCreateAdminDTO containing username, password, email, phoneNumber
     * @return success message
     */
    @Transactional
    public String createAdmin(AdminCreateAdminDTO dto) {
        // Validate username uniqueness
        if (userRepository.findOneByUsername(dto.getUsername()).isPresent()) {
            throw new DuplicateUsernameException("Username already exists");
        }

        // Validate email uniqueness
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException("Email already exists");
        }

        // Create new admin user
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setRole(Role.ADMIN);
        user.setActivated(true);

        userRepository.save(user);

        return "Admin account created successfully";
    }


    // *** HIỂN THỊ THÔNG TIN CHI TIẾT USER THEO ROLE ***
    public List<AdminUserDetailDTO> getAdminUserDetails() {
        List<User> users = userRepository.findByRole(Role.ADMIN);
        return users.stream()
                .map(user -> new AdminUserDetailDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getRole().name(),
                    user.isActivated(),
                    user.getFacebookId(),
                    null,  // createdAt
                    null   // lastLogin
                ))
                .collect(Collectors.toList());
    }    
    
    
    public List<AdminUserDetailDTO> getSellerUserDetails() {
        List<User> users = userRepository.findByRole(Role.SELLER);
        return users.stream()
                .map(user -> {
                    // Lấy shop của seller
                    Optional<Shop> shopOpt = shopRepository.findByOwnerId(user.getId());
                    
                    // ✅ Lấy địa chỉ STORE từ bảng addresses (TypeAddress.STORE)
                    Optional<Address> storeAddress = addressRepository.findFirstByUserIdAndTypeAddress(
                        user.getId(), 
                        com.PBL6.Ecommerce.constant.TypeAddress.STORE
                    );
                    
                    String shopName = null;
                    String shopAddress = null;
                    String shopDescription = null;
                    String shopStatus = null;
                    int totalProductsSeller = 0;
                    int totalOrdersSeller = 0;
                    double totalRevenue = 0.0;
                    
                    if (shopOpt.isPresent()) {
                        Shop shop = shopOpt.get();
                        shopName = shop.getName();
                        shopDescription = shop.getDescription();
                        shopStatus = shop.getStatus() != null ? shop.getStatus().name() : null;
                        
                        // ✅ Ghép các trường địa chỉ thành 1 chuỗi hoàn chỉnh
                        shopAddress = storeAddress.map(addr -> {
                            StringBuilder sb = new StringBuilder();
                            if (addr.getFullAddress() != null && !addr.getFullAddress().trim().isEmpty()) {
                                sb.append(addr.getFullAddress());
                            }
                            if (addr.getWardName() != null && !addr.getWardName().trim().isEmpty()) {
                                if (sb.length() > 0) sb.append(", ");
                                sb.append(addr.getWardName());
                            }
                            if (addr.getDistrictName() != null && !addr.getDistrictName().trim().isEmpty()) {
                                if (sb.length() > 0) sb.append(", ");
                                sb.append(addr.getDistrictName());
                            }
                            if (addr.getProvinceName() != null && !addr.getProvinceName().trim().isEmpty()) {
                                if (sb.length() > 0) sb.append(", ");
                                sb.append(addr.getProvinceName());
                            }
                            return sb.length() > 0 ? sb.toString() : null;
                        }).orElse(null);
                        
                        // Đếm tổng sản phẩm của shop
                        totalProductsSeller = (int) productRepository.countByShopId(shop.getId());
                        
                        // Đếm số đơn hàng COMPLETED của shop
                        totalOrdersSeller = (int) orderRepository.countCompletedOrdersByShopId(shop.getId());
                        
                        // Tính tổng doanh thu từ các đơn hàng COMPLETED
                        Double revenue = orderRepository.getTotalRevenueByShopId(shop.getId());
                        totalRevenue = revenue != null ? revenue : 0.0;
                    }
                    
                    return new AdminUserDetailDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getRole().name(),
                        user.isActivated(),
                        user.getCreatedAt(),
                        shopName,
                        shopAddress,
                        shopDescription,
                        shopStatus,
                        totalProductsSeller,
                        totalOrdersSeller,
                        totalRevenue
                    );
                })
                .collect(Collectors.toList());
    }

    public List<AdminUserDetailDTO> getCustomerUserDetails() {
        List<User> users = userRepository.findByRole(Role.BUYER);
        return users.stream()
                .map(user -> {
                    // ✅ Lấy địa chỉ HOME của customer
                    Optional<Address> homeAddress = addressRepository.findFirstByUserIdAndTypeAddress(
                        user.getId(), 
                        com.PBL6.Ecommerce.constant.TypeAddress.HOME
                    );
                    
                    // ✅ Tính thông tin từ Orders (CHỈ đơn COMPLETED)
                    long totalOrders = orderRepository.countCompletedOrdersByUserId(user.getId());
                    Double totalSpent = orderRepository.getTotalSpentByUserId(user.getId());
                    LocalDateTime lastOrderDate = orderRepository.getLastCompletedOrderDateByUserId(user.getId()).orElse(null);
                    
                    // Đếm số sản phẩm trong giỏ hàng
                    int cartItemsCount = cartRepository.findByUserId(user.getId())
                            .map(cart -> cartItemRepository.findByCartId(cart.getId()).size())
                            .orElse(0);
                    
                    // ✅ Ghép các trường địa chỉ thành 1 chuỗi hoàn chỉnh
                    String fullAddressString = homeAddress.map(addr -> {
                        StringBuilder sb = new StringBuilder();
                        if (addr.getFullAddress() != null && !addr.getFullAddress().trim().isEmpty()) {
                            sb.append(addr.getFullAddress());
                        }
                        if (addr.getWardName() != null && !addr.getWardName().trim().isEmpty()) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(addr.getWardName());
                        }
                        if (addr.getDistrictName() != null && !addr.getDistrictName().trim().isEmpty()) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(addr.getDistrictName());
                        }
                        if (addr.getProvinceName() != null && !addr.getProvinceName().trim().isEmpty()) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(addr.getProvinceName());
                        }
                        return sb.length() > 0 ? sb.toString() : null;
                    }).orElse(null);
                    
                    return new AdminUserDetailDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getRole().name(),
                        user.isActivated(),
                        user.getCreatedAt(),
                        (int) totalOrders,
                        totalSpent,
                        lastOrderDate,
                        cartItemsCount,
                        fullAddressString,  // ✅ Ghép tất cả thành 1 trường
                        homeAddress.map(Address::getProvinceName).orElse(null),
                        homeAddress.map(Address::getDistrictName).orElse(null),
                        homeAddress.map(Address::getWardName).orElse(null),
                        homeAddress.map(Address::getContactPhone).orElse(null)
                    );
                })
                .collect(Collectors.toList());
    }

    public AdminUserDetailDTO getUserDetailById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        
        if (user.getRole() == Role.ADMIN) {
            return new AdminUserDetailDTO(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole().name(),
                user.isActivated(),
                user.getFacebookId(),
                null,
                null
            );
        } else if (user.getRole() == Role.SELLER) {
            // Lấy shop của seller
            Optional<Shop> shopOpt = shopRepository.findByOwnerId(user.getId());
            
            // ✅ Lấy địa chỉ STORE từ bảng addresses (TypeAddress.STORE)
            Optional<Address> storeAddress = addressRepository.findFirstByUserIdAndTypeAddress(
                user.getId(), 
                com.PBL6.Ecommerce.constant.TypeAddress.STORE
            );
            
            String shopName = null;
            String shopAddress = null;
            String shopDescription = null;
            String shopStatus = null;
            int totalProductsSeller = 0;
            int totalOrdersSeller = 0;
            double totalRevenue = 0.0;
            
            if (shopOpt.isPresent()) {
                Shop shop = shopOpt.get();
                shopName = shop.getName();
                shopDescription = shop.getDescription();
                shopStatus = shop.getStatus() != null ? shop.getStatus().name() : null;
                
                // ✅ Ghép các trường địa chỉ thành 1 chuỗi hoàn chỉnh
                shopAddress = storeAddress.map(addr -> {
                    StringBuilder sb = new StringBuilder();
                    if (addr.getFullAddress() != null && !addr.getFullAddress().trim().isEmpty()) {
                        sb.append(addr.getFullAddress());
                    }
                    if (addr.getWardName() != null && !addr.getWardName().trim().isEmpty()) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(addr.getWardName());
                    }
                    if (addr.getDistrictName() != null && !addr.getDistrictName().trim().isEmpty()) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(addr.getDistrictName());
                    }
                    if (addr.getProvinceName() != null && !addr.getProvinceName().trim().isEmpty()) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(addr.getProvinceName());
                    }
                    return sb.length() > 0 ? sb.toString() : null;
                }).orElse(null);
                
                // Đếm tổng sản phẩm của shop
                totalProductsSeller = (int) productRepository.countByShopId(shop.getId());
                
                // Đếm số đơn hàng COMPLETED của shop
                totalOrdersSeller = (int) orderRepository.countCompletedOrdersByShopId(shop.getId());
                
                // Tính tổng doanh thu từ các đơn hàng COMPLETED
                Double revenue = orderRepository.getTotalRevenueByShopId(shop.getId());
                totalRevenue = revenue != null ? revenue : 0.0;
            }
            
            return new AdminUserDetailDTO(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole().name(),
                user.isActivated(),
                shopOpt.isPresent() ? shopOpt.get().getCreatedAt() : user.getCreatedAt(), // Lấy từ shop.createdAt
                shopName,
                shopAddress,
                shopDescription,
                shopStatus,
                totalProductsSeller,
                totalOrdersSeller,
                totalRevenue
            );
        } else {
            // BUYER/CUSTOMER - ✅ Lấy địa chỉ HOME từ bảng addresses
            Optional<Address> homeAddress = addressRepository.findFirstByUserIdAndTypeAddress(
                user.getId(), 
                com.PBL6.Ecommerce.constant.TypeAddress.HOME
            );
            
            // ✅ Tính thông tin từ Orders (CHỈ đơn COMPLETED)
            long totalOrders = orderRepository.countCompletedOrdersByUserId(user.getId());
            Double totalSpent = orderRepository.getTotalSpentByUserId(user.getId());
            LocalDateTime lastOrderDate = orderRepository.getLastCompletedOrderDateByUserId(user.getId()).orElse(null);
            
            // Đếm số sản phẩm trong giỏ hàng
            int cartItemsCount = cartRepository.findByUserId(user.getId())
                    .map(cart -> cartItemRepository.findByCartId(cart.getId()).size())
                    .orElse(0);
            
            // ✅ Ghép các trường địa chỉ thành 1 chuỗi hoàn chỉnh
            String fullAddressString = homeAddress.map(addr -> {
                StringBuilder sb = new StringBuilder();
                if (addr.getFullAddress() != null && !addr.getFullAddress().trim().isEmpty()) {
                    sb.append(addr.getFullAddress());
                }
                if (addr.getWardName() != null && !addr.getWardName().trim().isEmpty()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(addr.getWardName());
                }
                if (addr.getDistrictName() != null && !addr.getDistrictName().trim().isEmpty()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(addr.getDistrictName());
                }
                if (addr.getProvinceName() != null && !addr.getProvinceName().trim().isEmpty()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(addr.getProvinceName());
                }
                return sb.length() > 0 ? sb.toString() : null;
            }).orElse(null);
            
            return new AdminUserDetailDTO(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole().name(),
                user.isActivated(),
                user.getCreatedAt(),
                (int) totalOrders,
                totalSpent,
                lastOrderDate,
                cartItemsCount,
                fullAddressString,  // ✅ Ghép tất cả thành 1 trường
                homeAddress.map(Address::getProvinceName).orElse(null),
                homeAddress.map(Address::getDistrictName).orElse(null),
                homeAddress.map(Address::getWardName).orElse(null),
                homeAddress.map(Address::getContactPhone).orElse(null)
            );
        }
    }

    
    /**
     * Xóa user và tất cả dữ liệu liên quan
     * - Nếu là ADMIN (role = 0): xóa địa chỉ trong bảng addresses
     * - Nếu là SELLER (role = 1): xóa shop, products, vouchers, và địa chỉ
     * - Nếu là BUYER (role = 2): xóa cart, cart items, và địa chỉ
     * - Xóa verification codes
     */
    @Transactional
    public void deleteUser(Long userId) {
        log.debug("Deleting user with ID: {}", userId);
        
        // 1. Tìm user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found");
                });
        
        // 2. Không cho phép xóa chính mình
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (user.getUsername().equals(currentUsername)) {
            log.warn("User attempted to delete their own account: {}", currentUsername);
            throw new UnauthorizedUserActionException("Cannot delete your own account");
        }
        
        // 3. Kiểm tra xem có phải admin cuối cùng không
        if (user.getRole() == Role.ADMIN) {
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if (adminCount <= 1) {
                log.warn("Attempted to delete the last admin in the system");
                throw new UserHasReferencesException("Cannot delete the last admin in the system");
            }
        }
        
        // 4. Xóa dữ liệu chung cho tất cả user (orders, reviews, refresh tokens, wallet)
        log.info("Deleting common data for user ID: {}", userId);
        deleteUserOrders(userId);
        deleteUserReviews(userId);
        deleteUserRefreshTokens(user);
        deleteUserWallet(userId);
        
        // 5. Xóa dữ liệu theo role
        switch (user.getRole()) {
            case ADMIN:
                // ADMIN (role = 0): Xóa địa chỉ trong bảng addresses
                log.info("Deleting ADMIN data for user ID: {}", userId);
                deleteUserAddresses(userId);
                break;
                
            case SELLER:
                // SELLER (role = 1): Xóa shop, products, vouchers, và địa chỉ
                log.info("Deleting SELLER data for user ID: {}", userId);
                deleteSellerShopAndProducts(userId);
                deleteUserAddresses(userId);
                break;
                
            case BUYER:
                // BUYER (role = 2): Xóa cart, cart items, và địa chỉ
                log.info("Deleting BUYER data for user ID: {}", userId);
                deleteUserCart(userId);
                deleteUserAddresses(userId);
                break;
        }
        
        // 6. Xóa verification codes
        deleteUserVerifications(user);
        
        // 7. Xóa user khỏi bảng users
        userRepository.delete(user);
        
        log.info("User deleted successfully - User ID: {}, Username: {}, Role: {}", 
            userId, user.getUsername(), user.getRole());
    }
    
    /**
     * Xóa tất cả địa chỉ của user trong bảng addresses
     */
    private void deleteUserAddresses(Long userId) {
        log.debug("Deleting all addresses for user ID: {}", userId);
        List<Address> addresses = addressRepository.findByUserId(userId);
        
        if (!addresses.isEmpty()) {
            addressRepository.deleteAll(addresses);
            log.info("Deleted {} addresses for user ID: {}", addresses.size(), userId);
        }
    }
    
    /**
     * Xóa cart và cart items của BUYER (role = 2)
     */
    private void deleteUserCart(Long userId) {
        log.debug("Deleting cart for BUYER user ID: {}", userId);
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            
            // Xóa tất cả cart items trước
            cartItemRepository.deleteByCartId(cart.getId());
            log.info("Deleted cart items for cart ID: {}", cart.getId());
            
            // Xóa cart
            cartRepository.delete(cart);
            log.info("Deleted cart for user ID: {}", userId);
        }
    }
    
    /**
     * Xóa shop, products và vouchers của SELLER (role = 1)
     */
    private void deleteSellerShopAndProducts(Long userId) {
        log.debug("Deleting shop, products and vouchers for SELLER user ID: {}", userId);
        Optional<Shop> shopOpt = shopRepository.findByUserId(userId);
        
        if (shopOpt.isPresent()) {
            Shop shop = shopOpt.get();
            Long shopId = shop.getId();
            
            // 1. Xóa tất cả vouchers của shop
            vouchersRepository.deleteByShopId(shopId);
            log.info("Deleted vouchers for shop ID: {}", shopId);
            
            // 2. Xóa tất cả products của shop
            productRepository.deleteByUserId(userId);
            log.info("Deleted products for shop ID: {}", shopId);
            
            // 3. Xóa shop
            shopRepository.delete(shop);
            log.info("Deleted shop ID: {} for user ID: {}", shopId, userId);
        }
    }
    
    /**
     * Xóa tất cả orders của user (cả buyer và seller)
     */
    private void deleteUserOrders(Long userId) {
        log.debug("Deleting all orders for user ID: {}", userId);
        
        // Xóa orders nơi user là buyer
        List<Order> buyerOrders = orderRepository.findByUserId(userId);
        if (!buyerOrders.isEmpty()) {
            orderRepository.deleteAll(buyerOrders);
            log.info("Deleted {} orders where user ID {} was buyer", buyerOrders.size(), userId);
        }
        
        // Xóa orders nơi user là seller (through shop)
        Optional<Shop> shopOpt = shopRepository.findByUserId(userId);
        if (shopOpt.isPresent()) {
            List<Order> sellerOrders = orderRepository.findByShopId(shopOpt.get().getId());
            if (!sellerOrders.isEmpty()) {
                orderRepository.deleteAll(sellerOrders);
                log.info("Deleted {} orders where user ID {} was seller", sellerOrders.size(), userId);
            }
        }
    }
    
    /**
     * Xóa tất cả product reviews của user
     */
    private void deleteUserReviews(Long userId) {
        log.debug("Deleting all product reviews for user ID: {}", userId);
        
        // Tìm tất cả reviews của user
        List<ProductReview> reviews = 
            productReviewRepository.findAll().stream()
                .filter(review -> review.getUser().getId().equals(userId))
                .collect(Collectors.toList());
        
        if (!reviews.isEmpty()) {
            productReviewRepository.deleteAll(reviews);
            log.info("Deleted {} product reviews for user ID: {}", reviews.size(), userId);
        }
    }
    
    /**
     * Xóa tất cả refresh tokens của user
     */
    private void deleteUserRefreshTokens(User user) {
        log.debug("Deleting all refresh tokens for user: {}", user.getUsername());
        
        int deletedCount = refreshTokenRepository.deleteByUser(user);
        if (deletedCount > 0) {
            log.info("Deleted {} refresh tokens for user ID: {}", deletedCount, user.getId());
        }
    }
    
    /**
     * Xóa wallet của user
     */
    private void deleteUserWallet(Long userId) {
        log.debug("Deleting wallet for user ID: {}", userId);
        
        Optional<Wallet> walletOpt = walletRepository.findByUserId(userId);
        if (walletOpt.isPresent()) {
            walletRepository.delete(walletOpt.get());
            log.info("Deleted wallet for user ID: {}", userId);
        }
    }

    /**
     * Admin update user information (for BUYER and ADMIN roles only)
     * Can update: username, email, phone, activated status
     * @param userId - ID of user to update
     * @param dto - DTO containing fields to update
     * @return Updated UserInfoDTO
     * @throws UserNotFoundException if user not found
     * @throws InvalidRoleException if user is SELLER (use seller update API instead)
     * @throws DuplicateEmailException if email already exists
     * @throws DuplicatePhoneException if phone already exists
     */
    @Transactional
    public UserInfoDTO updateUserInfo(Long userId, AdminUpdateUserDTO dto) {
        log.info("Admin updating user ID: {}", userId);
        
        // Find user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        
        // Validate role - only allow BUYER and ADMIN
        if (user.getRole() == Role.SELLER) {
            throw new InvalidRoleException("Cannot update seller using this API. Use seller update API instead.");
        }
        
        // Update username if provided
        if (dto.getUsername() != null && !dto.getUsername().trim().isEmpty()) {
            String newUsername = dto.getUsername().trim();
            
            // Check if username already exists (excluding current user)
            Optional<User> existingUser = userRepository.findByUsername(newUsername);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new DuplicateEmailException("Username already exists: " + newUsername);
            }
            
            user.setUsername(newUsername);
            log.debug("Updated username to: {}", newUsername);
        }
        
        // Update email if provided
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            String newEmail = dto.getEmail().trim().toLowerCase();
            
            // Check if email already exists (excluding current user)
            Optional<User> existingUser = userRepository.findByEmail(newEmail);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new DuplicateEmailException("Email already exists: " + newEmail);
            }
            
            user.setEmail(newEmail);
            log.debug("Updated email to: {}", newEmail);
        }
        
        // Update phone if provided
        if (dto.getPhone() != null && !dto.getPhone().trim().isEmpty()) {
            String newPhone = dto.getPhone().trim();
            
            // Check if phone already exists (excluding current user)
            Optional<User> existingUser = userRepository.findOneByPhoneNumber(newPhone);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new DuplicatePhoneException("Phone number already exists: " + newPhone);
            }
            
            user.setPhoneNumber(newPhone);
            log.debug("Updated phone to: {}", newPhone);
        }
        
        // Update activated status if provided
        if (dto.getActivated() != null) {
            user.setActivated(dto.getActivated());
            log.debug("Updated activated status to: {}", dto.getActivated());
        }
        
        // Save updated user
        User updatedUser = userRepository.save(user);
        log.info("Successfully updated user ID: {}", userId);
        
        // Return UserInfoDTO
        return new UserInfoDTO(
            updatedUser.getId(),
            updatedUser.getEmail(),
            updatedUser.getUsername(),
            updatedUser.getRole().name()
        );
    }

    /**
     * ADMIN UPDATE SELLER (shop and user profile)
     * @param userId - ID of seller user
     * @param dto - DTO containing shop and user update data
     * @return UserInfoDTO with updated information
     * @throws UserNotFoundException if user not found
     * @throws InvalidRoleException if user is not a seller
     * @throws DuplicateEmailException if email already exists
     * @throws DuplicatePhoneException if phone already exists
     */
    @Transactional
    public UserInfoDTO updateSellerInfo(Long userId, AdminUpdateSellerDTO dto) {
        log.info("Admin updating seller ID: {}", userId);
        
        // Find user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        
        // Validate role - only allow SELLER
        if (user.getRole() != Role.SELLER) {
            throw new InvalidRoleException("User is not a seller. Cannot update using seller API.");
        }
        
        // Get seller's shop
        Shop shop = user.getShop();
        if (shop == null) {
            throw new IllegalStateException("Seller does not have a shop");
        }
        
        // Track if shop status changed
        ShopStatus oldStatus = shop.getStatus();
        ShopStatus newStatus = oldStatus;
        
        // Update shop fields
        if (dto.getShopStatus() != null && !dto.getShopStatus().trim().isEmpty()) {
            try {
                newStatus = ShopStatus.valueOf(dto.getShopStatus().toUpperCase());
                shop.setStatus(newStatus);
                log.debug("Updated shop status from {} to {}", oldStatus, newStatus);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid shop status: " + dto.getShopStatus() + 
                    ". Must be one of: ACTIVE, INACTIVE, PENDING");
            }
        }
        
        if (dto.getShopName() != null && !dto.getShopName().trim().isEmpty()) {
            shop.setName(dto.getShopName().trim());
            log.debug("Updated shop name to: {}", dto.getShopName());
        }
        
        if (dto.getShopDescription() != null) {
            shop.setDescription(dto.getShopDescription().trim());
            log.debug("Updated shop description");
        }
        
        // Synchronize product status when shop status changes
        if (newStatus != oldStatus) {
            synchronizeProductStatus(shop, oldStatus, newStatus);
        }
        
        // Update user fields
        if (dto.getUsername() != null && !dto.getUsername().trim().isEmpty()) {
            String newUsername = dto.getUsername().trim();
            
            // Check if username already exists (excluding current user)
            Optional<User> existingUser = userRepository.findOneByUsername(newUsername);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new DuplicateUsernameException("Username already exists: " + newUsername);
            }
            
            user.setUsername(newUsername);
            log.debug("Updated username to: {}", newUsername);
        }
        
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            String newEmail = dto.getEmail().trim().toLowerCase();
            
            // Validate email format
            if (!newEmail.endsWith("@gmail.com")) {
                throw new IllegalArgumentException("Email must end with @gmail.com");
            }
            
            // Check if email already exists (excluding current user)
            Optional<User> existingUser = userRepository.findByEmail(newEmail);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new DuplicateEmailException("Email already exists: " + newEmail);
            }
            
            user.setEmail(newEmail);
            log.debug("Updated email to: {}", newEmail);
        }
        
        if (dto.getPhone() != null && !dto.getPhone().trim().isEmpty()) {
            String newPhone = dto.getPhone().trim();
            
            // Validate phone format (must start with 0 and have exactly 10 digits)
            if (!newPhone.matches("^0\\d{9}$")) {
                throw new IllegalArgumentException("Phone must start with 0 and have exactly 10 digits");
            }
            
            // Check if phone already exists (excluding current user)
            Optional<User> existingUser = userRepository.findOneByPhoneNumber(newPhone);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new DuplicatePhoneException("Phone number already exists: " + newPhone);
            }
            
            user.setPhoneNumber(newPhone);
            log.debug("Updated phone to: {}", newPhone);
        }
        
        if (dto.getFullName() != null && !dto.getFullName().trim().isEmpty()) {
            user.setFullName(dto.getFullName().trim());
            log.debug("Updated full name to: {}", dto.getFullName());
        }
        
        // Save updated shop and user
        shopRepository.save(shop);
        User updatedUser = userRepository.save(user);
        log.info("Successfully updated seller ID: {} and shop ID: {}", userId, shop.getId());
        
        // Return UserInfoDTO
        return new UserInfoDTO(
            updatedUser.getId(),
            updatedUser.getEmail(),
            updatedUser.getUsername(),
            updatedUser.getRole().name()
        );
    }
    
    /**
     * Đồng bộ hóa trạng thái is_active của sản phẩm khi trạng thái cửa hàng thay đổi
     * @param shop - The shop whose products need synchronization
     * @param oldStatus - Previous shop status
     * @param newStatus - New shop status
     */
    private void synchronizeProductStatus(Shop shop, ShopStatus oldStatus, ShopStatus newStatus) {
        log.info("Synchronizing products for shop ID: {} (status change: {} -> {})", 
            shop.getId(), oldStatus, newStatus);
        
        List<Product> products = productRepository.findByShop(shop);
        
        if (products.isEmpty()) {
            log.debug("No products found for shop ID: {}", shop.getId());
            return;
        }
        
        int updatedCount = 0;
        
        // If shop becomes ACTIVE, activate all inactive products
        if (newStatus == ShopStatus.ACTIVE && 
            (oldStatus == ShopStatus.PENDING || oldStatus == ShopStatus.INACTIVE)) {
            
            for (Product product : products) {
                if (!product.getIsActive()) {
                    product.setIsActive(true);
                    updatedCount++;
                }
            }
            
            if (updatedCount > 0) {
                productRepository.saveAll(products);
                log.info("Activated {} products for shop ID: {}", updatedCount, shop.getId());
            }
        }
        // If shop becomes INACTIVE or PENDING, deactivate all active products
        else if ((newStatus == ShopStatus.INACTIVE || newStatus == ShopStatus.PENDING) && 
                 oldStatus == ShopStatus.ACTIVE) {
            
            for (Product product : products) {
                if (product.getIsActive()) {
                    product.setIsActive(false);
                    updatedCount++;
                }
            }
            
            if (updatedCount > 0) {
                productRepository.saveAll(products);
                log.info("Deactivated {} products for shop ID: {}", updatedCount, shop.getId());
            }
        }
        
        if (updatedCount == 0) {
            log.debug("No product status updates needed for shop ID: {}", shop.getId());
        }
    }

    /**
     * Lấy thông tin hồ sơ của quản trị viên
    * Chỉ trả về dữ liệu hồ sơ cơ bản cho quản trị viên đã xác thực
     */
    public AdminMyProfileDTO getAdminMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthenticated access attempt");
            throw new UnauthenticatedException("Authentication required");
        }

        String principal = authentication.getName();
        Optional<User> userOpt = Optional.empty();

        // Try to find user by id (if principal is numeric)
        if (principal != null && principal.matches("^\\d+$")) {
            try {
                Long id = Long.parseLong(principal);
                userOpt = userRepository.findById(id);
            } catch (NumberFormatException ignored) { }
        }

        // Try by username or email
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findOneByUsername(principal);
        }
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findOneByEmail(principal);
        }

        User user = userOpt.orElseThrow(() -> {
            log.warn("Admin user not found for principal: {}", principal);
            return new UserNotFoundException("Admin user not found");
        });

        // Verify user is actually an admin
        if (!user.getRole().equals(Role.ADMIN)) {
            log.warn("Non-admin user attempted to access admin profile: {}", user.getUsername());
            throw new UnauthorizedUserActionException("Access denied: Admin role required");
        }

        return new AdminMyProfileDTO(
            user.getId(),
            user.getUsername(),
            user.getFullName(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getAvatarUrl(),
            user.isActivated(),
            user.getCreatedAt()
        );
    }

    /**
     * Update admin's own profile information (excluding avatar)
     * Avatar upload is handled separately via multipart endpoint
     */
    @Transactional
    public AdminMyProfileDTO updateAdminMyProfile(AdminUpdateMyProfileDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthenticated access attempt");
            throw new UnauthenticatedException("Authentication required");
        }

        User user = resolveCurrentUser(authentication);

        // Verify user is actually an admin
        if (!user.getRole().equals(Role.ADMIN)) {
            log.warn("Non-admin user attempted to update admin profile: {}", user.getUsername());
            throw new UnauthorizedUserActionException("Access denied: Admin role required");
        }

        // Check for duplicate username (excluding current user)
        if (!user.getUsername().equals(dto.getUsername())) {
            if (userRepository.findOneByUsername(dto.getUsername()).isPresent()) {
                throw new DuplicateUsernameException("Username already exists: " + dto.getUsername());
            }
        }

        // Check for duplicate email (excluding current user)
        if (!user.getEmail().equals(dto.getEmail())) {
            if (userRepository.findOneByEmail(dto.getEmail()).isPresent()) {
                throw new DuplicateEmailException("Email already exists: " + dto.getEmail());
            }
        }

        // Check for duplicate phone number (excluding current user)
        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.findOneByPhoneNumber(dto.getPhoneNumber()).isPresent()) {
                throw new DuplicatePhoneException("Phone number already exists: " + dto.getPhoneNumber());
            }
        }

        // Update fields
        user.setUsername(dto.getUsername());
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());

        userRepository.save(user);
        log.info("Admin profile updated successfully for user ID: {}", user.getId());

        return new AdminMyProfileDTO(
            user.getId(),
            user.getUsername(),
            user.getFullName(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getAvatarUrl(),
            user.isActivated(),
            user.getCreatedAt()
        );
    }

    /**
     * Change admin's password
     */
    @Transactional
    public void changeAdminPassword(AdminChangePasswordDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthenticated access attempt");
            throw new UnauthenticatedException("Authentication required");
        }

        User user = resolveCurrentUser(authentication);

        // Verify user is actually an admin
        if (!user.getRole().equals(Role.ADMIN)) {
            log.warn("Non-admin user attempted to change admin password: {}", user.getUsername());
            throw new UnauthorizedUserActionException("Access denied: Admin role required");
        }

        // Verify old password matches
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new PasswordMismatchException("Old password is incorrect");
        }

        // Verify new password and confirm password match
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new PasswordMismatchException("New password and confirm password do not match");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        
        log.info("Admin password changed successfully for user ID: {}", user.getId());
    }

    /**
     * Upload admin avatar
     * This should be called from controller with multipart file handling
     */
    @Transactional
    public AdminMyProfileDTO updateAdminAvatar(String avatarUrl) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthenticated access attempt");
            throw new UnauthenticatedException("Authentication required");
        }

        User user = resolveCurrentUser(authentication);

        // Verify user is actually an admin
        if (!user.getRole().equals(Role.ADMIN)) {
            log.warn("Non-admin user attempted to update admin avatar: {}", user.getUsername());
            throw new UnauthorizedUserActionException("Access denied: Admin role required");
        }

        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        
        log.info("Admin avatar updated successfully for user ID: {}", user.getId());

        return new AdminMyProfileDTO(
            user.getId(),
            user.getUsername(),
            user.getFullName(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getAvatarUrl(),
            user.isActivated(),
            user.getCreatedAt()
        );
    }


}
