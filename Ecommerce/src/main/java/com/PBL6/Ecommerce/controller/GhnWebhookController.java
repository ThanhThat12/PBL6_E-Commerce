package com.PBL6.Ecommerce.controller;

import org.springframework.web.bind.annotation.*;
import com.PBL6.Ecommerce.domain.Shipment;
import com.PBL6.Ecommerce.repository.ShipmentRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@RestController
@RequestMapping("/api/webhook/ghn")
public class GhnWebhookController {

    private final ShipmentRepository shipmentRepository;

    public GhnWebhookController(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    /**
     * Bước 14: GHN webhook cập nhật trạng thái
     * POST /api/webhook/ghn/status
     */
    @PostMapping("/status")
    @Transactional
    public Map<String,Object> updateStatus(@RequestBody Map<String,Object> payload) {
        try {
            String orderCode = (String) payload.get("OrderCode");
            String status = (String) payload.get("Status");
            
            Shipment shipment = shipmentRepository.findByGhnOrderCode(orderCode)
                .orElse(null);
            
            if (shipment != null) {
                shipment.setStatus(status);
                shipmentRepository.save(shipment);
            }
            
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }
}