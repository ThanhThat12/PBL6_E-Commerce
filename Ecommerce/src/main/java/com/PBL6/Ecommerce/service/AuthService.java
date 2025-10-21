package com.PBL6.Ecommerce.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.LoginDTO;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.util.TokenProvider;

@Service

public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final LoginAttemptService loginAttemptService; // Rate limiting service

    public AuthService(UserRepository userRepository, 
                       PasswordEncoder passwordEncoder, 
                       TokenProvider tokenProvider,
                       LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.loginAttemptService = loginAttemptService;
    }

    public String authenticate(LoginDTO dto) {
        String username = dto.getUsername().toLowerCase();
        
        // Prompt 2: Check rate limiting for login (5 attempts per 15 minutes per IP)
        // Note: IP address should be extracted from HttpServletRequest in controller
        // For now, using username as a simple identifier. In production, use IP from request.
        String clientIdentifier = username; // In controller, replace with IP from request
        
        if (!loginAttemptService.isLoginAttemptAllowed(clientIdentifier)) {
            loginAttemptService.recordLoginAttempt(clientIdentifier);
            int remainingAttempts = loginAttemptService.getRemainingLoginAttempts(clientIdentifier);
            if (remainingAttempts == 0) {
                loginAttemptService.lockIp(clientIdentifier);
            }
            throw new RuntimeException("Đăng nhập thất bại quá nhiều lần. Vui lòng thử lại sau 15 phút.");
        }
        
        Optional<User> userOpt = userRepository.findOneByUsername(username);
        if (userOpt.isEmpty()) {
            loginAttemptService.recordLoginAttempt(clientIdentifier);
            throw new RuntimeException("Invalid username or password");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            loginAttemptService.recordLoginAttempt(clientIdentifier);
            int remainingAttempts = loginAttemptService.getRemainingLoginAttempts(clientIdentifier);
            if (remainingAttempts == 0) {
                loginAttemptService.lockIp(clientIdentifier);
            }
            throw new RuntimeException("Invalid username or password");
        }

        if (!user.isActivated()) {
            throw new RuntimeException("User not activated");
        }

        String token = tokenProvider.createToken(user.getUsername(), user.getRole().name());
        
        // Clear login attempts after successful authentication
        loginAttemptService.clearLoginAttempts(clientIdentifier);

        return token;
    }
}