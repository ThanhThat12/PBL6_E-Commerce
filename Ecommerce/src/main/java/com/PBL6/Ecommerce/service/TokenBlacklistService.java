package com.PBL6.Ecommerce.service;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

/**
 * Token Blacklist Service for token revocation
 * Stores revoked JTI (JWT ID) values to prevent their use after logout
 * Prompt 4: Token Revocation & Logout functionality
 * 
 * Note: This is an in-memory implementation suitable for single-instance deployments
 * For production multi-instance deployments, use Redis or database storage
 */
@Service
public class TokenBlacklistService {
    
    // Key: JTI (JWT ID), Value: Expiration timestamp in milliseconds
    private final ConcurrentHashMap<String, Long> blacklist = new ConcurrentHashMap<>();
    
    // Key: User ID, Value: Timestamp of logout-all command (revoke all tokens issued before this time)
    private final ConcurrentHashMap<Long, Long> userLogoutAllTimestamps = new ConcurrentHashMap<>();
    
    /**
     * Add a JTI to the blacklist with expiration time
     * @param jti JWT ID to blacklist
     * @param expirationTime Token expiration time in milliseconds
     */
    public void blacklistToken(String jti, long expirationTime) {
        blacklist.put(jti, expirationTime);
    }
    
    /**
     * Check if a JTI is blacklisted
     * @param jti JWT ID to check
     * @return true if JTI is blacklisted and not yet expired
     */
    public boolean isTokenBlacklisted(String jti) {
        Long expirationTime = blacklist.get(jti);
        
        if (expirationTime == null) {
            return false; // Token not in blacklist
        }
        
        long currentTime = System.currentTimeMillis();
        
        if (currentTime > expirationTime) {
            // Token has expired, remove from blacklist
            blacklist.remove(jti);
            return false;
        }
        
        return true; // Token is blacklisted and still valid
    }
    
    /**
     * Logout all devices for a user (revoke all tokens)
     * @param userId User ID
     */
    public void logoutAllDevices(Long userId) {
        userLogoutAllTimestamps.put(userId, System.currentTimeMillis());
    }
    
    /**
     * Check if a token was issued before user's logout-all command
     * @param userId User ID
     * @param tokenIssuedAt Token issue timestamp in milliseconds
     * @return true if token was issued before logout-all command
     */
    public boolean isTokenInvalidatedByLogoutAll(Long userId, long tokenIssuedAt) {
        Long logoutAllTime = userLogoutAllTimestamps.get(userId);
        
        if (logoutAllTime == null) {
            return false; // No logout-all command for this user
        }
        
        return tokenIssuedAt < logoutAllTime; // Token issued before logout-all
    }
    
    /**
     * Cleanup expired tokens from blacklist (should be called periodically)
     */
    public void cleanupExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        blacklist.entrySet().removeIf(entry -> currentTime > entry.getValue());
    }
    
    /**
     * Get current blacklist size (for monitoring)
     */
    public int getBlacklistSize() {
        return blacklist.size();
    }
    
    /**
     * Clear entire blacklist (use cautiously - only for testing/maintenance)
     */
    public void clearBlacklist() {
        blacklist.clear();
        userLogoutAllTimestamps.clear();
    }
}
