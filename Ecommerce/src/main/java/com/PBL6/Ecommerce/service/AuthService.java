package com.PBL6.Ecommerce.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.LoginDTO;
import com.PBL6.Ecommerce.exception.InvalidCredentialsException;
import com.PBL6.Ecommerce.exception.UserNotActivatedException;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.util.TokenProvider;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public String authenticate(LoginDTO dto) {
        log.debug("Authenticating user: {}", dto.getUsername());
        
        Optional<User> userOpt = userRepository.findOneByUsername(dto.getUsername().toLowerCase());
        if (userOpt.isEmpty()) {
            log.warn("Login failed - user not found: {}", dto.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("Login failed - incorrect password for user: {}", dto.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        if (!user.isActivated()) {
            log.warn("Login failed - user not activated: {}", dto.getUsername());
            throw new UserNotActivatedException("User account is not activated");
        }

        log.info("User authenticated successfully: {}", dto.getUsername());
        String token = tokenProvider.createToken(user.getId(), user.getUsername(), user.getEmail(), List.of(user.getRole().name()));
        return token;
    }
}
