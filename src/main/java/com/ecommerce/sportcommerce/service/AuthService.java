package com.ecommerce.sportcommerce.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.sportcommerce.security.UserPrincipal;
import com.ecommerce.sportcommerce.dto.request.LoginRequest;
import com.ecommerce.sportcommerce.dto.request.LogoutRequest;
import com.ecommerce.sportcommerce.dto.request.RefreshTokenRequest;
import com.ecommerce.sportcommerce.dto.request.RegisterRequest;
import com.ecommerce.sportcommerce.dto.request.ResendOtpRequest;
import com.ecommerce.sportcommerce.dto.request.VerifyOtpRequest;
import com.ecommerce.sportcommerce.dto.response.AuthResponse;
import com.ecommerce.sportcommerce.dto.response.UserResponse;
import com.ecommerce.sportcommerce.entity.OtpVerification;
import com.ecommerce.sportcommerce.entity.RefreshToken;
import com.ecommerce.sportcommerce.entity.User;
import com.ecommerce.sportcommerce.exception.BadRequestException;
import com.ecommerce.sportcommerce.exception.EmailAlreadyExistsException;
import com.ecommerce.sportcommerce.exception.UnauthorizedException;
import com.ecommerce.sportcommerce.repository.UserRepository;

/**
 * Service implementation for authentication operations
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            OtpService otpService,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Step 1: Register - Send OTP
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail();

        // Check if email already exists with LOCAL provider
        if (userRepository.existsByEmailAndProvider(email, User.Provider.LOCAL)) {
            throw new EmailAlreadyExistsException("Email đã được đăng ký");
        }

        // Check if username already exists (if provided)
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BadRequestException("Username đã tồn tại");
            }
        }

        // Check if phone already exists (if provided)
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new BadRequestException("Số điện thoại đã được sử dụng");
            }
        }

        // Generate and send OTP
        otpService.generateAndSendOtp(email, OtpVerification.OtpType.REGISTRATION);

        logger.info("Registration OTP sent to: {}", email);

        return AuthResponse.builder()
                .email(email)
                .expiresInMinutes(otpService.getOtpExpirationMinutes())
                .build();
    }

    /**
     * Step 2: Verify OTP and complete registration
     */
    @Transactional
    public AuthResponse verifyOtpAndCreateUser(VerifyOtpRequest request) {
        // Verify OTP
        OtpVerification otp = otpService.verifyOtp(
                request.getEmail(),
                request.getOtpCode(),
                OtpVerification.OtpType.REGISTRATION
        );

        // Get original registration data (from request)
        String email = request.getEmail();
        String password = request.getPassword();

        // Hash password
        String hashedPassword = passwordEncoder.encode(password);

        // Create user
        User user = User.builder()
                .email(email)
                .password(hashedPassword)
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(User.Role.BUYER)
                .status(User.Status.ACTIVE)
                .provider(User.Provider.LOCAL)
                .emailVerified(true)
                .emailVerifiedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);
        logger.info("User created successfully: {}", user.getEmail());

        // Generate JWT tokens
        String accessToken = jwtService.generateToken(new UserPrincipal(user));
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // Build response
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime() / 1000)
                .user(mapToUserResponse(user))
                .build();
    }

    /**
     * Resend OTP
     */
    @Transactional
    public AuthResponse resendOtp(ResendOtpRequest request) {
        OtpVerification.OtpType otpType;

        try {
            otpType = OtpVerification.OtpType.valueOf(request.getOtpType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Loại OTP không hợp lệ");
        }

        // Generate and send OTP
        otpService.generateAndSendOtp(request.getEmail(), otpType);

        logger.info("OTP resent to: {}, type: {}", request.getEmail(), otpType);

        return AuthResponse.builder()
                .email(request.getEmail())
                .expiresInMinutes(otpService.getOtpExpirationMinutes())
                .build();
    }

    /**
     * Login with email and password
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();
        
        // Find user by email and LOCAL provider
        User user = userRepository.findByEmailAndProvider(email, User.Provider.LOCAL)
                .orElseThrow(() -> new BadRequestException("Email hoặc mật khẩu không đúng"));
        
        // Check account status
        if (user.getStatus() == User.Status.SUSPENDED) {
            throw new UnauthorizedException("Tài khoản đã bị tạm khóa");
        }
        
        if (user.getStatus() == User.Status.DELETED) {
            throw new UnauthorizedException("Tài khoản không tồn tại");
        }
        
        // Check email verified
        if (!user.getEmailVerified()) {
            throw new UnauthorizedException("Email chưa được xác thực. Vui lòng xác thực email trước khi đăng nhập");
        }
        
        // Check account lock
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
            String lockedUntilStr = user.getLockedUntil().format(formatter);
            throw new UnauthorizedException("Tài khoản bị khóa đến " + lockedUntilStr);
        }
        
        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            // Increase failed login attempts
            int failedAttempts = (user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0) + 1;
            user.setFailedLoginAttempts(failedAttempts);
            
            // Lock account if attempts >= 5
            if (failedAttempts >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
                userRepository.save(user);
                throw new BadRequestException("Tài khoản đã bị khóa 15 phút do nhập sai mật khẩu quá nhiều lần");
            }
            
            userRepository.save(user);
            throw new BadRequestException("Email hoặc mật khẩu không đúng");
        }
        
        // Reset failed login attempts
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Generate tokens
        UserPrincipal userPrincipal = new UserPrincipal(user);
        String accessToken = jwtService.generateToken(userPrincipal);
        
        // Create refresh token with remember me consideration
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, request.getRememberMe());
        
        logger.info("User logged in successfully: {}", email);
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime() / 1000)
                .user(mapToUserResponse(user))
                .build();
    }
    
    /**
     * Refresh access token using refresh token
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshTokenStr = request.getRefreshToken();
        
        // Verify refresh token
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenStr);
        User user = refreshToken.getUser();
        
        // Generate new access token
        UserPrincipal userPrincipal = new UserPrincipal(user);
        String newAccessToken = jwtService.generateToken(userPrincipal);
        
        // Generate new refresh token
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user, false);
        
        // Revoke old refresh token
        refreshTokenService.revokeToken(refreshTokenStr, newRefreshToken.getToken());
        
        logger.info("Token refreshed for user: {}", user.getEmail());
        
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime() / 1000)
                .user(mapToUserResponse(user))
                .build();
    }
    
    /**
     * Logout user
     */
    @Transactional
    public void logout(User user, LogoutRequest request) {
        if (request.getRefreshToken() != null && !request.getRefreshToken().isEmpty()) {
            // Revoke specific refresh token
            refreshTokenService.revokeTokenByString(request.getRefreshToken());
            logger.info("User logged out (specific token): {}", user.getEmail());
        } else {
            // Revoke all user tokens
            refreshTokenService.revokeAllUserTokens(user);
            logger.info("User logged out (all tokens): {}", user.getEmail());
        }
    }

    /**
     * Map User entity to UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .provider(user.getProvider().name())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
