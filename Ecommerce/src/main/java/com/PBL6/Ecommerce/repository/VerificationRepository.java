package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.Verification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationRepository extends JpaRepository<Verification, Long> {
    Optional<Verification> findTopByContactOrderByCreatedAtDesc(String contact);
    boolean existsByContactAndVerifiedTrue(String contact);
}
