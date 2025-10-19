package com.PBL6.Ecommerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.PBL6.Ecommerce.domain.Verification;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, Long> {
    Optional<Verification> findTopByContactOrderByCreatedAtDesc(String contact);
    boolean existsByContactAndVerifiedTrue(String contact);
    
    // Delete verifications where expiryTime is before given time and not verified / not used
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM Verification v WHERE v.expiryTime < :cutoff AND v.verified = false AND v.isUsed = false")
    int deleteExpiredUnusedVerifications(java.time.LocalDateTime cutoff);
}
