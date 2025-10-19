package com.PBL6.Ecommerce.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.domain.RefreshToken;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.repository.RefreshTokenRepository;
import com.PBL6.Ecommerce.repository.UserRepository;

/**
 * Service for managing refresh tokens
 * Handles creation, validation, revocation, and cleanup of refresh tokens
 */
@Service
public class RefreshTokenService {
    
    @Value("${refresh.token.expiration:604800000}")
    private long refreshTokenExpirationMs; // Default: 7 days in milliseconds
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                              UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Create a new refresh token for a user
     * @param userId User ID
     * @param ipAddress Client IP address
     * @param userAgent User agent string
     * @return Generated refresh token
     */
    public RefreshToken createRefreshToken(Long userId, String ipAddress, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Generate unique token
        String token = UUID.randomUUID().toString();
        
        // Calculate expiry date
        LocalDateTime expiryDate = LocalDateTime.now()
                .plusSeconds(refreshTokenExpirationMs / 1000);
        
        RefreshToken refreshToken = new RefreshToken(token, user, expiryDate, ipAddress, userAgent);
        return refreshTokenRepository.save(refreshToken);
    }
    
    /**
     * Find and validate a refresh token
     * @param token Token string
     * @return Optional containing the refresh token if valid
     */
    public Optional<RefreshToken> findValidToken(String token) {
        return refreshTokenRepository.findValidByToken(token)
                .filter(rt -> !rt.isExpired());
    }
    
    /**
     * Check if a refresh token is valid
     * @param token Token string
     * @return true if token exists, is not revoked, and is not expired
     */
    public boolean isTokenValid(String token) {
        return findValidToken(token).isPresent();
    }

    /**
     * Find refresh token by token string (raw lookup, returns even if revoked/expired)
     * @param token Token string
     * @return Optional containing the refresh token
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
    
    /**
     * Revoke a specific refresh token
     * @param token Token string
     */
    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                });
    }
    
    /**
     * Revoke all tokens for a user (logout from all devices)
     * @param userId User ID
     */
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllUserTokens(userId);
    }
    
    /**
     * Rotate a refresh token (revoke old, create new)
     * @param token Old token
     * @param ipAddress Client IP address
     * @param userAgent User agent string
     * @return New refresh token
     */
    @Transactional
    public RefreshToken rotateToken(String token, String ipAddress, String userAgent) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
        
        // Revoke old token
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);
        
        // Create new token for same user
        return createRefreshToken(oldToken.getUser().getId(), ipAddress, userAgent);
    }
    
    /**
     * Delete expired tokens (cleanup job)
     */
    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
    
    /**
     * Get refresh token by ID
     * @param id Token ID
     * @return Optional containing the refresh token
     */
    public Optional<RefreshToken> getTokenById(Long id) {
        return refreshTokenRepository.findById(id);
    }
}
