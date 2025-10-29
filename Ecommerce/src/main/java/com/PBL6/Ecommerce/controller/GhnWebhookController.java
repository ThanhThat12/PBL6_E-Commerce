package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.Shipment;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.repository.ShipmentRepository;
import com.PBL6.Ecommerce.service.GhnService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/ghn")
public class GhnWebhookController {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final GhnService ghnService;

    public GhnWebhookController(ShipmentRepository shipmentRepository, OrderRepository orderRepository, GhnService ghnService) {
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
        this.ghnService = ghnService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String,Object> payload,
                                                @RequestHeader Map<String,String> headers) {
        try {
            Object dataObj = payload.get("data");
            if (!(dataObj instanceof Map)) return ResponseEntity.badRequest().body("no data");
            Map<?,?> data = (Map<?,?>) dataObj;

            Object orderCode = data.get("order_code");
            Object clientOrderCode = data.get("client_order_code");
            Object statusId = data.get("status_id");

            if (orderCode != null) {
                Optional<Shipment> os = shipmentRepository.findByGhnOrderCode(String.valueOf(orderCode));
                if (os.isPresent()) {
                    Shipment s = os.get();
                    s.setStatus(String.valueOf(statusId));
                    s.setGhnPayload(ghnService.toJson(data));
                    shipmentRepository.save(s);
                }
            }

            if (clientOrderCode != null) {
                String code = String.valueOf(clientOrderCode);
                if (code.startsWith("ORDER_")) {
                    try {
                        Long orderId = Long.parseLong(code.substring(6));
                        Optional<Order> o = orderRepository.findById(orderId);
                        if (o.isPresent()) {
                            Order order = o.get();
                            // map GHN status_id to internal status (simple)
                            String st = String.valueOf(statusId);
                            if ("3".equals(st) || "7".equals(st)) order.setStatus(Order.OrderStatus.COMPLETED);
                            else if ("5".equals(st)) order.setStatus(Order.OrderStatus.CANCELLED);
                            else order.setStatus(Order.OrderStatus.PROCESSING);
                            orderRepository.save(order);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("error");
        }
        return ResponseEntity.ok("ok");
    }
}