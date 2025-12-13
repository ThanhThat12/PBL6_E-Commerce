package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.UserFCMToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFCMTokenRepository extends JpaRepository<UserFCMToken, Long> {
    
    Optional<UserFCMToken> findByFcmToken(String fcmToken);
    
    List<UserFCMToken> findByUserId(Long userId);
    
    @Query("SELECT t FROM UserFCMToken t WHERE t.userId = ?1 AND t.isActive = true")
    List<UserFCMToken> findActiveTokensByUserId(Long userId);
    
    @Modifying
    @Transactional
    @Query("UPDATE UserFCMToken t SET t.isActive = false WHERE t.userId = ?1")
    void deactivateAllTokensByUserId(Long userId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM UserFCMToken t WHERE t.userId = ?1")
    void deleteByUserId(Long userId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM UserFCMToken t WHERE t.fcmToken = ?1")
    void deleteByFcmToken(String fcmToken);
}
