package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.entity.notification.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {
    
    /**
     * Find all active tokens for a user
     */
    List<UserFcmToken> findByUser_IdAndIsActiveTrue(Long userId);
    
    /**
     * Find token by user and device
     */
    Optional<UserFcmToken> findByUser_IdAndDeviceId(Long userId, String deviceId);
    
    /**
     * Deactivate token by device ID
     */
    @Modifying
    @Query("UPDATE UserFcmToken t SET t.isActive = false WHERE t.user.id = :userId AND t.deviceId = :deviceId")
    int deactivateToken(@Param("userId") Long userId, @Param("deviceId") String deviceId);
    
    /**
     * Deactivate all tokens for a user
     */
    @Modifying
    @Query("UPDATE UserFcmToken t SET t.isActive = false WHERE t.user.id = :userId")
    int deactivateAllUserTokens(@Param("userId") Long userId);
    
    /**
     * Delete inactive tokens older than specified days
     */
    @Modifying
    @Query("DELETE FROM UserFcmToken t WHERE t.isActive = false AND t.updatedAt < :cutoffDate")
    int deleteInactiveTokens(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}
