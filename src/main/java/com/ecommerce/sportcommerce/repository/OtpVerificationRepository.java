package com.ecommerce.sportcommerce.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.sportcommerce.entity.OtpVerification;

/**
 * Repository interface for OtpVerification entity
 */
@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    
    /**
     * Find the latest unverified OTP for email and type that hasn't expired
     */
    @Query("SELECT o FROM OtpVerification o WHERE o.email = :email " +
           "AND o.otpType = :otpType " +
           "AND o.verified = false " +
           "AND o.expiresAt > :currentTime " +
           "ORDER BY o.createdAt DESC")
    Optional<OtpVerification> findLatestValidOtp(
        @Param("email") String email,
        @Param("otpType") OtpVerification.OtpType otpType,
        @Param("currentTime") LocalDateTime currentTime
    );
    
    /**
     * Find latest OTP by email and type (regardless of verification status)
     */
    @Query("SELECT o FROM OtpVerification o WHERE o.email = :email " +
           "AND o.otpType = :otpType " +
           "ORDER BY o.createdAt DESC")
    Optional<OtpVerification> findLatestOtpByEmailAndType(
        @Param("email") String email,
        @Param("otpType") OtpVerification.OtpType otpType
    );
    
    /**
     * Delete all unverified OTPs for email and type
     */
    @Modifying
    @Query("DELETE FROM OtpVerification o WHERE o.email = :email " +
           "AND o.otpType = :otpType " +
           "AND o.verified = false")
    void deleteUnverifiedOtpsByEmailAndType(
        @Param("email") String email,
        @Param("otpType") OtpVerification.OtpType otpType
    );
    
    /**
     * Delete expired OTPs (cleanup job)
     */
    @Modifying
    @Query("DELETE FROM OtpVerification o WHERE o.expiresAt < :currentTime")
    int deleteExpiredOtps(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Count unverified OTPs created after a certain time (for rate limiting)
     */
    @Query("SELECT COUNT(o) FROM OtpVerification o WHERE o.email = :email " +
           "AND o.otpType = :otpType " +
           "AND o.createdAt > :afterTime")
    long countRecentOtps(
        @Param("email") String email,
        @Param("otpType") OtpVerification.OtpType otpType,
        @Param("afterTime") LocalDateTime afterTime
    );
}
