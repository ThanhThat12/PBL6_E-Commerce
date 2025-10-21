package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.Verification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, Long> {
    Optional<Verification> findByContactAndOtpAndExpiryTimeAfter(
        String contact, String otp, LocalDateTime currentTime
    );
    
    List<Verification> findByContact(String contact);
    
    @Modifying
    @Query("DELETE FROM Verification v WHERE v.contact = :contact")
    void deleteByContact(@Param("contact") String contact);

    Optional<Verification> findTopByContactOrderByCreatedAtDesc(String contact);
}
