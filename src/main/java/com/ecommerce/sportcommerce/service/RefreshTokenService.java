package com.ecommerce.sportcommerce.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.sportcommerce.entity.RefreshToken;
import com.ecommerce.sportcommerce.entity.User;
import com.ecommerce.sportcommerce.exception.BadRequestException;
import com.ecommerce.sportcommerce.repository.RefreshTokenRepository;

/**
 * Service for managing refresh tokens
 */
@Service
public class RefreshTokenService {
    
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);
    
    private final RefreshTokenRepository refreshTokenRepository;
    
    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;
    
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }
    
    /**
     * Create refresh token for user
     */
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        return createRefreshToken(user, false);
    }
    
    /**
     * Create refresh token for user with remember me option
     */
    @Transactional
    public RefreshToken createRefreshToken(User user, Boolean rememberMe) {
        // Revoke old tokens
        refreshTokenRepository.revokeAllUserTokens(user);
        
        // Create new token
        String token = UUID.randomUUID().toString();
        
        // Calculate expiration: 7 days normal, 30 days if remember me
        long expirationSeconds = rememberMe ? (30L * 24 * 60 * 60) : (refreshExpiration / 1000);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(expirationSeconds))
                .revoked(false)
                .build();
        
        refreshTokenRepository.save(refreshToken);
        logger.info("Refresh token created for user: {} (rememberMe: {})", user.getEmail(), rememberMe);
        
        return refreshToken;
    }
    
    /**
     * Find refresh token by token string
     */
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Refresh token không hợp lệ"));
    }
    
    /**
     * Verify refresh token is valid
     */
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = findByToken(token);
        
        if (!refreshToken.isValid()) {
            logger.warn("Invalid refresh token attempted: {}", token);
            throw new BadRequestException("Refresh token không hợp lệ hoặc đã hết hạn");
        }
        
        return refreshToken;
    }
    
    /**
     * Revoke refresh token
     */
    @Transactional
    public void revokeToken(String token) {
        RefreshToken refreshToken = findByToken(token);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        logger.info("Refresh token revoked: {}", token);
    }
    
    /**
     * Revoke refresh token and set replaced by
     */
    @Transactional
    public void revokeToken(String token, String replacedByToken) {
        RefreshToken refreshToken = findByToken(token);
        refreshToken.setRevoked(true);
        refreshToken.setReplacedByToken(replacedByToken);
        refreshTokenRepository.save(refreshToken);
        logger.info("Refresh token revoked and replaced: {}", token);
    }
    
    /**
     * Revoke refresh token by string (for logout)
     */
    @Transactional
    public void revokeTokenByString(String token) {
        try {
            revokeToken(token);
        } catch (BadRequestException e) {
            logger.warn("Attempted to revoke invalid token: {}", token);
            // Ignore if token doesn't exist
        }
    }
    
    /**
     * Revoke all user tokens
     */
    @Transactional
    public void revokeAllUserTokens(User user) {
        int revokedCount = refreshTokenRepository.revokeAllUserTokens(user);
        logger.info("Revoked {} refresh tokens for user: {}", revokedCount, user.getEmail());
    }
    
    /**
     * Cleanup expired tokens (scheduled job)
     */
    @Transactional
    public int cleanupExpiredTokens() {
        int deletedCount = refreshTokenRepository.deleteExpiredOrRevokedTokens(LocalDateTime.now());
        logger.info("Cleaned up {} expired/revoked refresh tokens", deletedCount);
        return deletedCount;
    }
}
