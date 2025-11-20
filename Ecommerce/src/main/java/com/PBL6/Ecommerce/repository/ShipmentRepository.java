package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByGhnOrderCode(String ghnOrderCode);
    Optional<Shipment> findByOrderId(Long orderId);
}