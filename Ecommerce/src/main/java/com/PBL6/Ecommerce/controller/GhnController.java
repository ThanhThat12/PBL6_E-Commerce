package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.domain.Shipment;
import com.PBL6.Ecommerce.domain.dto.CreateOrderRequestDTO;
import com.PBL6.Ecommerce.repository.ShipmentRepository;
import com.PBL6.Ecommerce.service.GhnService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ghn")
@Validated
public class GhnController {

    private final GhnService ghnService;
    private final ShipmentRepository shipmentRepository;

    public GhnController(GhnService ghnService, ShipmentRepository shipmentRepository) {
        this.ghnService = ghnService;
        this.shipmentRepository = shipmentRepository;
    }

    @PostMapping("/fee")
    public ResponseEntity<?> calculateFee(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(ghnService.calculateFee(payload));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createShipment(@RequestBody Map<String, Object> payload,
                                            @RequestParam(name = "orderId", required = false) Long orderId) {
        Map<String, Object> resp = ghnService.createShippingOrder(payload);
        // nếu client gửi orderId thì lưu vào shipments
        if (orderId != null) {
            Shipment s = new Shipment();
            s.setOrderId(orderId);
            Object data = resp.get("data");
            if (data instanceof Map) {
                Object orderCode = ((Map<?,?>) data).get("order_code");
                if (orderCode != null) s.setGhnOrderCode(String.valueOf(orderCode));
            }
            s.setGhnPayload(ghnService.toJson(resp));
            s.setStatus("CREATED");
            shipmentRepository.save(s);
        }
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/detail/{orderCode}")
    public ResponseEntity<?> detail(@PathVariable String orderCode) {
        return ResponseEntity.ok(ghnService.getOrderDetail(orderCode));
    }

    // convenience endpoint: create order + GHN (optional minimal flow).
    @PostMapping("/order")
public ResponseEntity<?> createOrderWithGhn(@Valid @RequestBody CreateOrderRequestDTO req) {
    // build mutable payload and include required_note
    Map<String,Object> payload = new HashMap<>();
    payload.put("to_name", req.getReceiverName());
    payload.put("to_phone", req.getReceiverPhone());
    payload.put("to_district_id", Integer.parseInt(req.getToDistrictId()));
    payload.put("to_ward_code", req.getToWardCode());
    payload.put("to_address", req.getReceiverAddress());
    payload.put("weight", req.getWeightGrams());
    payload.put("client_order_code", "ORDER_" + (req.getOrderReference() != null ? req.getOrderReference() : "TEMP"));
    payload.put("required_note", "KHONGCHOXEMHANG"); // bắt buộc bởi GHN -> thay theo yêu cầu nếu cần
    if (req.getCodAmount() != null) payload.put("cod_amount", req.getCodAmount().intValue());
    // items nếu có
    if (req.getItems() != null) {
        payload.put("items", req.getItems().stream().map(i -> {
            Map<String,Object> m = new HashMap<>();
            m.put("name", "Item-" + i.getVariantId());
            m.put("quantity", i.getQuantity());
            m.put("price", 0);
            return m;
        }).collect(Collectors.toList()));
    }

    Map<String,Object> resp = ghnService.createShippingOrder(payload);
    // optional: persist Shipment if client provided orderId
    if (req.getOrderReference() != null) {
        Shipment s = new Shipment();
        try {
            s.setOrderId(Long.parseLong(req.getOrderReference()));
        } catch (NumberFormatException ignored) {}
        s.setGhnPayload(ghnService.toJson(resp));
        shipmentRepository.save(s);
    }
    return ResponseEntity.ok(resp);
}
}