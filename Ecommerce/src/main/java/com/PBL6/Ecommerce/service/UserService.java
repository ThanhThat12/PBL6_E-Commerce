package com.PBL6.Ecommerce.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.domain.Cart;
import com.PBL6.Ecommerce.domain.Role;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.Verification;
import com.PBL6.Ecommerce.domain.dto.AdminUserDetailDTO;
import com.PBL6.Ecommerce.domain.dto.CheckContactDTO;
import com.PBL6.Ecommerce.domain.dto.ListAdminUserDTO;
import com.PBL6.Ecommerce.domain.dto.ListCustomerUserDTO;
import com.PBL6.Ecommerce.domain.dto.ListSellerUserDTO;
import com.PBL6.Ecommerce.domain.dto.RegisterDTO;
import com.PBL6.Ecommerce.domain.dto.UserInfoDTO;
import com.PBL6.Ecommerce.domain.dto.VerifyOtpDTO;
import com.PBL6.Ecommerce.repository.CartItemRepository;
import com.PBL6.Ecommerce.repository.CartRepository;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.repository.VerificationRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final VerificationRepository verificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SmsService smsService;
    private final LoginAttemptService loginAttemptService;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;

    public UserService(UserRepository userRepository,
                       VerificationRepository verificationRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService,
                       SmsService smsService,
                       LoginAttemptService loginAttemptService,
                       CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ShopRepository shopRepository,
                       ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.smsService = smsService;
        this.loginAttemptService = loginAttemptService;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.shopRepository = shopRepository;
        this.productRepository = productRepository;
    }

    // Locks per contact to avoid concurrent verification insertions
    private final ConcurrentMap<String, Object> contactLocks = new ConcurrentHashMap<>();

    public String checkContact(CheckContactDTO dto) {
        String contact = dto.getContact();

        // Check if contact is rate-limited for OTP resend
        if (!loginAttemptService.isOtpResendAllowed(contact)) {
            throw new RuntimeException("Bạn đã gửi OTP quá nhiều lần. Vui lòng thử lại sau 15 phút.");
        }

        // Time-based cooldown: prevent quick consecutive resends (60 seconds)
        Optional<Verification> lastOpt = verificationRepository.findTopByContactOrderByCreatedAtDesc(contact);
        if (lastOpt.isPresent()) {
            Verification last = lastOpt.get();
            // Prefer explicit lastResendTime if present, otherwise fallback to createdAt (backwards compatibility)
            java.time.LocalDateTime ref = last.getLastResendTime() != null ? last.getLastResendTime() : last.getCreatedAt();
            // If we can't determine a reliable timestamp, deny resend to be safe and avoid creating duplicate rows
            if (ref == null) {
                throw new RuntimeException("Vui lòng đợi 60 giây trước khi yêu cầu mã OTP mới.");
            }
            long secondsSince = java.time.Duration.between(ref, LocalDateTime.now()).getSeconds();
            if (secondsSince < 60) {
                throw new RuntimeException("Vui lòng đợi " + (60 - secondsSince) + " giây trước khi yêu cầu mã OTP mới.");
            }
        }

        // 1. Kiểm tra tồn tại
        if (contact.contains("@")) {
            if (userRepository.existsByEmail(contact)) {
                throw new RuntimeException("Email đã tồn tại");
            }
        } else {
            if (userRepository.existsByPhoneNumber(contact)) {
                throw new RuntimeException("Số điện thoại đã tồn tại");
            }
        }


        // 2. Sinh OTP and save inside a per-contact synchronized block to avoid duplicate DB inserts
        String otp = String.format("%06d", new Random().nextInt(999999));

        Object lock = contactLocks.computeIfAbsent(contact, k -> new Object());
        synchronized (lock) {
            // Re-check latest verification inside lock to avoid race condition
            Optional<Verification> latest = verificationRepository.findTopByContactOrderByCreatedAtDesc(contact);
            if (latest.isPresent()) {
                Verification l = latest.get();
                java.time.LocalDateTime ref = l.getLastResendTime() != null ? l.getLastResendTime() : l.getCreatedAt();
                if (ref == null) {
                    throw new RuntimeException("Vui lòng đợi 60 giây trước khi yêu cầu mã OTP mới.");
                }
                long secondsSince = java.time.Duration.between(ref, LocalDateTime.now()).getSeconds();
                if (secondsSince < 60) {
                    throw new RuntimeException("Vui lòng đợi " + (60 - secondsSince) + " giây trước khi yêu cầu mã OTP mới.");
                }
            }

            Verification verification = new Verification(
                    contact,
                    otp,
                    LocalDateTime.now().plusMinutes(5),
                    false,
                    LocalDateTime.now()
            );
            verification.setFailedAttempts(0);
            verification.setUsed(false);
            verification.setLocked(false);
            verification.setLastResendTime(LocalDateTime.now()); // set cooldown timestamp
            verificationRepository.save(verification);

            // Record OTP resend attempt for rate limiting
            loginAttemptService.recordOtpResendAttempt(contact);
        }

        // 3. Gửi OTP
        if (contact.contains("@")) {
            emailService.sendOtp(contact, otp);
        } else {
            smsService.sendOtp(contact, otp);
        }

        return "OTP đã được gửi tới " + contact;
    }

    public String verifyOtp(VerifyOtpDTO dto) {
        String contact = dto.getContact();
        
        // Check if OTP verification is rate-limited (Prompt 2)
        if (!loginAttemptService.isOtpVerifyAllowed(contact)) {
            throw new RuntimeException("Bạn đã xác thực OTP quá nhiều lần. Vui lòng thử lại sau 15 phút.");
        }
        
        Optional<Verification> verificationOpt =
                verificationRepository.findTopByContactOrderByCreatedAtDesc(contact);

        if (verificationOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy OTP");
        }

        Verification verification = verificationOpt.get();
        
        // Prompt 3: Check if OTP is locked due to too many failed attempts
        if (verification.isLocked()) {
            throw new RuntimeException("OTP này đã bị khóa do nhiều lần xác thực thất bại. Vui lòng yêu cầu OTP mới.");
        }
        
        // Prompt 3: Check if OTP is already used
        if (verification.isUsed()) {
            throw new RuntimeException("OTP này đã được sử dụng rồi. Vui lòng yêu cầu OTP mới.");
        }
        
        // Check OTP value
        if (!verification.getOtp().equals(dto.getOtp())) {
            // Prompt 3: Increment failed attempts
            verification.setFailedAttempts(verification.getFailedAttempts() + 1);
            
            // Prompt 3: Lock OTP after 3 failed attempts
            if (verification.getFailedAttempts() >= 3) {
                verification.setLocked(true);
                verificationRepository.save(verification);
                loginAttemptService.recordOtpVerifyAttempt(contact);
                throw new RuntimeException("OTP không đúng. OTP đã bị khóa do 3 lần xác thực thất bại. Vui lòng yêu cầu OTP mới.");
            }
            
            verificationRepository.save(verification);
            loginAttemptService.recordOtpVerifyAttempt(contact);
            int remainingAttempts = 3 - verification.getFailedAttempts();
            throw new RuntimeException("OTP không đúng. Bạn còn " + remainingAttempts + " lần thử.");
        }
        
        // Check OTP expiry
        if (verification.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP đã hết hạn");
        }

        // Prompt 3: Mark OTP as used after successful verification
        verification.setVerified(true);
        verification.setUsed(true);
        verification.setFailedAttempts(0); // Reset failed attempts on success
        verificationRepository.save(verification);
        
        // Reset rate limiting for this contact after successful verification
        loginAttemptService.resetContactAttempts(contact);

        return "Xác thực thành công";
    }

    public String register(RegisterDTO dto) {
        Verification verification = verificationRepository
                .findTopByContactOrderByCreatedAtDesc(dto.getContact())
                .orElseThrow(() -> new RuntimeException("Chưa xác thực OTP"));

        if (!verification.isVerified()) {
            throw new RuntimeException("Bạn chưa xác thực OTP");
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu không khớp");
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
            throw new RuntimeException("Chưa đăng nhập");
        }

        String principal = authentication.getName();

        Optional<User> userOpt = userRepository.findOneByUsername(principal);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findOneByEmail(principal);
        }
        User user = userOpt.orElseThrow(() -> new RuntimeException("User not found"));

        return new UserInfoDTO(user.getId(), user.getEmail(), user.getUsername(), user.getPhoneNumber(), user.getRole().name());
    }


    public List<UserInfoDTO> getUsersByRole(String roleName) {
        Role role;
        try {
            role = Role.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Role không hợp lệ: " + roleName);
        }
        
        List<User> users = userRepository.findByRole(role);
        return users.stream()
                .map(user -> new UserInfoDTO(
                    user.getId(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getPhoneNumber(),
                    user.getRole().name()
                ))
                .collect(Collectors.toList());
    }


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
                null, // createdAt - thêm field này vào User entity nếu cần
                null  // lastLogin - thêm field này vào User entity nếu cần
            ))
            .collect(Collectors.toList());
    }

    public List<ListSellerUserDTO> getSellerUsers() {
        List<User> users = userRepository.findByRole(Role.SELLER);
        return users.stream()
                .map(user -> new ListSellerUserDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    null, // shopName - lấy từ Shop entity
                    null, // shopAddress - lấy từ Shop entity
                    user.isActivated(),
                    null, // registeredAt
                    0,    // totalProducts - đếm từ Product entity
                    0     // totalOrders - đếm từ Order entity
                ))
                .collect(Collectors.toList());
    }

    public List<ListCustomerUserDTO> getCustomerUsers() {
        List<User> users = userRepository.findByRole(Role.BUYER);
        return users.stream()
                .map(user -> new ListCustomerUserDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.isActivated(),
                    null,  // registeredAt
                    null,  // lastOrderDate - lấy từ Order entity
                    0,     // totalOrders - đếm từ Order entity
                    0.0    // totalSpent - tính từ Order entity
                ))
                .collect(Collectors.toList());
    }


    // *** HIỂN THỊ THÔNG TIN CHI TIẾT USER THEO ROLE ***
    public List<AdminUserDetailDTO> getAdminUserDetails() {
    List<User> users = userRepository.findByRole(Role.ADMIN);
    return users.stream()
            .map(user -> new AdminUserDetailDTO(
                user.getId(),
                user.getUsername(),
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
                .map(user -> new AdminUserDetailDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getRole().name(),
                    user.isActivated(),
                    null,  // createdAt
                    null,  // shopName
                    null,  // shopAddress
                    null,  // shopDescription
                    0,     // totalProducts
                    0,     // totalSales
                    0.0    // totalRevenue
                ))
                .collect(Collectors.toList());
    }

    public List<AdminUserDetailDTO> getCustomerUserDetails() {
        List<User> users = userRepository.findByRole(Role.BUYER);
        return users.stream()
                .map(user -> new AdminUserDetailDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getRole().name(),
                    user.isActivated(),
                    null,  // createdAt
                    0,     // totalOrders
                    0.0,   // totalSpent
                    null,  // lastOrderDate
                    0      // cartItemsCount
                ))
                .collect(Collectors.toList());
    }

    public AdminUserDetailDTO getUserDetailById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        
        if (user.getRole() == Role.ADMIN) {
            return new AdminUserDetailDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole().name(),
                user.isActivated(),
                user.getFacebookId(),
                null,
                null
            );
        } else if (user.getRole() == Role.SELLER) {
            return new AdminUserDetailDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole().name(),
                user.isActivated(),
                null,
                null, null, null, 0, 0, 0.0
            );
        } else {
            return new AdminUserDetailDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole().name(),
                user.isActivated(),
                null,
                0, 0.0, null, 0
            );
        }
    }




    //***ADMIN CẬP NHẬT ROLE VÀ TRẠNG THÁI USER***

    public UserInfoDTO updateUserRole(Long userId, String newRole) {
    // Tìm user
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User không tồn tại"));
    
    // Validate role
    Role role;
    try {
        role = Role.valueOf(newRole.toUpperCase());
    } catch (IllegalArgumentException e) {
        throw new RuntimeException("Role không hợp lệ. Chỉ chấp nhận: ADMIN, SELLER, BUYER");
    }
    
    // Không cho phép thay đổi role của chính mình
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    if (user.getUsername().equals(currentUsername)) {
        throw new RuntimeException("Không thể thay đổi role của chính mình");
    }
    
    // Cập nhật role
    user.setRole(role);
    userRepository.save(user);
    
    // Trả về UserInfoDTO
    return new UserInfoDTO(
        user.getId(),
        user.getEmail(),
        user.getUsername(),
        user.getPhoneNumber(),
        user.getRole().name()
    );
    }

    public UserInfoDTO updateUserStatus(Long userId, boolean activated) {
        // Tìm user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        
        // Không cho phép vô hiệu hóa chính mình
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (user.getUsername().equals(currentUsername)) {
            throw new RuntimeException("Không thể thay đổi trạng thái của chính mình");
        }
        
        // Cập nhật trạng thái
        user.setActivated(activated);
        userRepository.save(user);
        
        // Trả về UserInfoDTO
        return new UserInfoDTO(
            user.getId(),
            user.getEmail(),
            user.getUsername(),
            user.getPhoneNumber(),
            user.getRole().name()
        );
    }



    /**
     * Xóa user và tất cả dữ liệu liên quan
     * - Nếu là BUYER: xóa cart và cart items
     * - Nếu là SELLER: xóa shop và products
     * - Xóa verification codes
     */
    @Transactional
    public void deleteUser(Long userId) {
        // 1. Tìm user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        
        // 2. Không cho phép xóa chính mình
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (user.getUsername().equals(currentUsername)) {
            throw new RuntimeException("Không thể xóa tài khoản của chính mình");
        }
        
        // 3. Kiểm tra xem có phải admin cuối cùng không
        if (user.getRole() == Role.ADMIN) {
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if (adminCount <= 1) {
                throw new RuntimeException("Không thể xóa admin cuối cùng trong hệ thống");
            }
        }
        
        // 4. Xóa dữ liệu theo role
        switch (user.getRole()) {
            case BUYER:
                // Xóa cart và cart items của BUYER
                deleteUserCart(userId);
                break;
                
            case SELLER:
                // Xóa shop và products của SELLER
                deleteSellerShopAndProducts(userId);
                break;
                
            case ADMIN:
                // Admin không có dữ liệu đặc biệt cần xóa
                break;
        }
        
        // 5. Xóa verification codes
        deleteUserVerifications(user);
        
        // 6. Xóa user
        userRepository.delete(user);
    }
    
    /**
     * Xóa cart và cart items của BUYER
     */
    private void deleteUserCart(Long userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            
            // Xóa tất cả cart items trước
            cartItemRepository.deleteByCartId(cart.getId());
            
            // Xóa cart
            cartRepository.delete(cart);
        }
    }
    
    /**
     * Xóa shop và products của SELLER
     */
    private void deleteSellerShopAndProducts(Long userId) {
        Optional<Shop> shopOpt = shopRepository.findByUserId(userId);
        
        if (shopOpt.isPresent()) {
            Shop shop = shopOpt.get();
            
            // Xóa tất cả products của shop trước
            productRepository.deleteByUserId(userId);
            
            // Xóa shop
            shopRepository.delete(shop);
        }
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


    
}