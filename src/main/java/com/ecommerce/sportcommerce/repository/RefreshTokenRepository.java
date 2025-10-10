package com.ecommerce.sportcommerce.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import com.ecommerce.sportcommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.sportcommerce.entity.RefreshToken;

/**
 * Repository interface for RefreshToken entity
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<com.ecommerce.sportcommerce.entity.RefreshToken, Long> {
    
    /**
     * Find refresh token by token string
     */
    Optional<com.ecommerce.sportcommerce.entity.RefreshToken> findByToken(String token);
    
    /**
     * Find all active refresh tokens for a user
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user " +
           "AND rt.revoked = false " +
           "AND rt.expiresAt > :currentTime")
    Optional<com.ecommerce.sportcommerce.entity.RefreshToken> findActiveTokenByUser(
        @Param("user") com.ecommerce.sportcommerce.entity.User user,
        @Param("currentTime") LocalDateTime currentTime
    );
    
    /**
     * Revoke all tokens for a user (used during logout)
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true " +
           "WHERE rt.user = :user AND rt.revoked = false")
    int revokeAllUserTokens(@Param("user") com.ecommerce.sportcommerce.entity.User user);
    
    /**
     * Delete expired and revoked tokens (cleanup job)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :currentTime OR rt.revoked = true")
    int deleteExpiredOrRevokedTokens(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Count active tokens for a user
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user " +
           "AND rt.revoked = false " +
           "AND rt.expiresAt > :currentTime")
    long countActiveTokensByUser(
        @Param("user") com.ecommerce.sportcommerce.entity.User user,
        @Param("currentTime") LocalDateTime currentTime
    );
    
    /**
     * Delete all tokens for a user
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    void deleteAllByUser(@Param("user") User user);
}
