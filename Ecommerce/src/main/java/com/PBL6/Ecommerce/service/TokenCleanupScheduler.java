package com.PBL6.Ecommerce.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled cleanup service for expired tokens
 * Runs periodically to clean up expired refresh tokens and blacklisted JTIs
 */
@Service
public class TokenCleanupScheduler {
    
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    
    public TokenCleanupScheduler(RefreshTokenService refreshTokenService,
                                TokenBlacklistService tokenBlacklistService) {
        this.refreshTokenService = refreshTokenService;
        this.tokenBlacklistService = tokenBlacklistService;
    }
    
    /**
     * Clean up expired refresh tokens daily at 2 AM
     * Cron format: second minute hour day month weekday
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredRefreshTokens() {
        try {
            refreshTokenService.deleteExpiredTokens();
            System.out.println("[TokenCleanup] Expired refresh tokens cleaned up at " + LocalDateTime.now());
        } catch (Exception e) {
            System.err.println("[TokenCleanup] Error cleaning refresh tokens: " + e.getMessage());
        }
    }
    
    /**
     * Clean up expired blacklisted tokens hourly
     * This removes JTIs from blacklist once their expiration time has passed
     */
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredBlacklistedTokens() {
        try {
            tokenBlacklistService.cleanupExpiredTokens();
            System.out.println("[TokenCleanup] Expired blacklisted tokens cleaned up at " + LocalDateTime.now());
        } catch (Exception e) {
            System.err.println("[TokenCleanup] Error cleaning blacklist: " + e.getMessage());
        }
    }
}
