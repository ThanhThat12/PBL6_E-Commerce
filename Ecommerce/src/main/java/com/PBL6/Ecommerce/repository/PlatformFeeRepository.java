package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.entity.payment.PlatformFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlatformFeeRepository extends JpaRepository<PlatformFee, Long> {
    @Modifying
    @Query("DELETE FROM PlatformFee pf WHERE pf.order.id = :orderId")
    void deleteByOrderId(@Param("orderId") Long orderId);
    
    // Find platform fee by order ID
    @Query("SELECT pf FROM PlatformFee pf WHERE pf.order.id = :orderId")
    Optional<PlatformFee> findByOrderId(@Param("orderId") Long orderId);
}
