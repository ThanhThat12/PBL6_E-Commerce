package com.PBL6.Ecommerce.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.PBL6.Ecommerce.domain.dto.ChangePasswordDTO;
import com.PBL6.Ecommerce.domain.dto.CheckContactDTO;
import com.PBL6.Ecommerce.domain.dto.ListAdminUserDTO;
import com.PBL6.Ecommerce.domain.dto.ListCustomerUserDTO;
import com.PBL6.Ecommerce.domain.dto.ListSellerUserDTO;
import com.PBL6.Ecommerce.domain.dto.RegisterDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateProfileDTO;
import com.PBL6.Ecommerce.domain.dto.UserInfoDTO;
import com.PBL6.Ecommerce.domain.dto.UserListDTO;
import com.PBL6.Ecommerce.domain.dto.UserProfileDTO;
import com.PBL6.Ecommerce.domain.dto.VerifyOtpDTO;
import com.PBL6.Ecommerce.exception.DuplicateEmailException;
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
import com.PBL6.Ecommerce.repository.CartItemRepository;
import com.PBL6.Ecommerce.repository.CartRepository;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.repository.VerificationRepository;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final VerificationRepository verificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SmsService smsService;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;

    public UserService(UserRepository userRepository,
                       VerificationRepository verificationRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService,
                       SmsService smsService,
                       CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ShopRepository shopRepository,
                       ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.smsService = smsService;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.shopRepository = shopRepository;
        this.productRepository = productRepository;
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
        
        try {
            return Long.parseLong(jwt.getSubject());
        } catch (NumberFormatException e) {
            throw new UserNotFoundException("Invalid user ID in JWT token: " + jwt.getSubject());
        }
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

        return new UserInfoDTO(user.getId(), user.getEmail(), user.getUsername(), user.getRole().name());
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
        log.debug("Updating role for user ID: {} to role: {}", userId, newRole);
        
        // Tìm user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found");
                });
        
        // Validate role
        Role role;
        try {
            role = Role.valueOf(newRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role requested: {}", newRole);
            throw new InvalidRoleException("Invalid role. Only ADMIN, SELLER, BUYER are accepted");
        }
        
        // Không cho phép thay đổi role của chính mình
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (user.getUsername().equals(currentUsername)) {
            log.warn("User attempted to change their own role: {}", currentUsername);
            throw new UnauthorizedUserActionException("Cannot change your own role");
        }
    
    // Cập nhật role
    user.setRole(role);
    userRepository.save(user);
    
    log.info("User role updated successfully - User ID: {}, New Role: {}", userId, role);
    
    // Trả về UserInfoDTO
    return new UserInfoDTO(
        user.getId(),
        user.getEmail(),
        user.getUsername(),
        user.getRole().name()
    );
    }

    public UserInfoDTO updateUserStatus(Long userId, boolean activated) {
        log.debug("Updating status for user ID: {} to: {}", userId, activated);
        
        // Tìm user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found");
                });
        
        // Không cho phép vô hiệu hóa chính mình
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (user.getUsername().equals(currentUsername)) {
            log.warn("User attempted to change their own status: {}", currentUsername);
            throw new UnauthorizedUserActionException("Cannot change your own status");
        }
        
        // Cập nhật trạng thái
        user.setActivated(activated);
        userRepository.save(user);
        
        log.info("User status updated successfully - User ID: {}, Activated: {}", userId, activated);
        
        // Trả về UserInfoDTO
        return new UserInfoDTO(
            user.getId(),
            user.getEmail(),
            user.getUsername(),
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
        
        log.info("User deleted successfully - User ID: {}, Username: {}", userId, user.getUsername());
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
