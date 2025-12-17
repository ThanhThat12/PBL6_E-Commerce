// ...existing code...
package com.PBL6.Ecommerce.controller.shipping;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.dto.CreateOrderRequestDTO;
import com.PBL6.Ecommerce.domain.entity.order.Shipment;
import com.PBL6.Ecommerce.repository.ShipmentRepository;
import com.PBL6.Ecommerce.service.GhnService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "GHN Shipping", description = "GHN shipping integration - calculate fees, check available services, create shipments, and track orders. **Note:** Address changes (typeAddress field) do NOT affect GHN integration as GHN only uses provinceId, districtId, wardCode, and fullAddress.")
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

    @Operation(
        summary = "Calculate shipping fee",
        description = "Calculate GHN shipping fee based on destination, weight, and service type. Used during checkout to display shipping cost to customer. **Address typeAddress field is ignored - only provinceId, districtId, wardCode matter.**"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully calculated shipping fee",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Fee Response",
                    value = "{\"code\":200,\"message\":\"Success\",\"data\":{\"total\":22000,\"service_fee\":22000,\"insurance_fee\":0,\"pick_station_fee\":0,\"coupon_value\":0}}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid request - missing required fields")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Shipping fee calculation parameters",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Fee Request Example",
                value = "{\"service_type_id\":2,\"to_district_id\":1450,\"to_ward_code\":\"21012\",\"weight\":500,\"insurance_value\":0}"
            )
        )
    )
    @PostMapping("/fee")
    public ResponseEntity<?> calculateFee(
        @RequestBody Map<String, Object> payload,
        @Parameter(description = "Shop ID (optional - uses default if not provided)", example = "123")
        @RequestParam(name = "shopId", required = false) Long shopId
    ) {
        return ResponseEntity.ok(ghnService.calculateFee(payload, shopId));
    }

    @Operation(
        summary = "Get available shipping services",
        description = "Retrieve list of available GHN shipping services for the destination. Used to let customer choose shipping speed (standard, express, etc.)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved available services",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Services Response",
                    value = "{\"code\":200,\"data\":[{\"service_id\":53320,\"short_name\":\"Nhanh\",\"service_type_id\":2},{\"service_id\":53321,\"short_name\":\"Tiết kiệm\",\"service_type_id\":1}]}"
                )
            )
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Service availability check parameters",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Services Request",
                value = "{\"from_district\":1450,\"to_district\":1450}"
            )
        )
    )
    @PostMapping("/services")
    public ResponseEntity<?> availableServices(
        @RequestBody Map<String, Object> payload,
        @Parameter(description = "Shop ID (optional)", example = "123")
        @RequestParam(name = "shopId", required = false) Long shopId
    ) {
        return ResponseEntity.ok(ghnService.getAvailableServices(payload, shopId));
    }

    @Operation(
        summary = "Create GHN shipment",
        description = "Create a new shipping order with GHN. This registers the order with GHN and generates a tracking code. **Address data (provinceId, districtId, wardCode) is used here - typeAddress is NOT sent to GHN.**"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully created shipment",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Create Shipment Response",
                    value = "{\"code\":200,\"message\":\"Success\",\"data\":{\"order_code\":\"GHN123456\",\"sort_code\":\"1450-001\",\"trans_type\":\"truck\",\"ward_encode\":\"\",\"district_encode\":\"\",\"fee\":{\"main_service\":22000,\"insurance\":0,\"cod_fee\":0,\"station_do\":0,\"station_pu\":0,\"return\":0,\"r2s\":0,\"return_again\":0,\"coupon\":0,\"document_return\":0,\"double_check\":0,\"cod_failed_fee\":0,\"pick_remote_areas_fee\":0,\"deliver_remote_areas_fee\":0,\"cod_pick_remote_areas_fee\":0,\"total\":22000},\"total_fee\":22000,\"expected_delivery_time\":\"2025-12-20T23:59:59\"}}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid shipment data")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "GHN shipment creation payload",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Create Shipment Request",
                value = "{\"to_name\":\"Nguyen Van A\",\"to_phone\":\"0912345678\",\"to_address\":\"123 ABC Street\",\"to_ward_code\":\"21012\",\"to_district_id\":1450,\"weight\":500,\"length\":10,\"width\":10,\"height\":10,\"service_type_id\":2,\"payment_type_id\":1,\"required_note\":\"KHONGCHOXEMHANG\",\"items\":[{\"name\":\"Product A\",\"quantity\":1,\"price\":100000}],\"cod_amount\":100000,\"client_order_code\":\"ORDER_123\"}"
            )
        )
    )
    @PostMapping("/create")
    public ResponseEntity<?> createShipment(
        @RequestBody Map<String, Object> payload,
        @Parameter(description = "Order ID to link shipment (optional)", example = "456")
        @RequestParam(name = "orderId", required = false) Long orderId,
        @Parameter(description = "Shop ID (optional)", example = "123")
        @RequestParam(name = "shopId", required = false) Long shopId
    ) {
        Map<String, Object> resp = ghnService.createShippingOrder(payload, shopId);
        if (orderId != null) {
            Shipment s = new Shipment();
            s.setOrderId(orderId);
            Object data = resp.get("data");
            if (data instanceof Map) {
                Object orderCode = ((Map<?, ?>) data).get("order_code");
                if (orderCode != null) s.setGhnOrderCode(String.valueOf(orderCode));
                Object totalFee = ((Map<?, ?>) data).get("total_fee");
                // Shipping fee now set on Order, not Shipment
            }
            s.setGhnPayload(ghnService.toJson(resp));
            s.setStatus("CREATED");
            shipmentRepository.save(s);
        }
        return ResponseEntity.ok(resp);
    }

    @Operation(
        summary = "Get shipment details",
        description = "Retrieve detailed information about a shipment using GHN order code. Used for tracking shipment status and history."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved shipment details",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Shipment Detail Response",
                    value = "{\"code\":200,\"data\":{\"order_code\":\"GHN123456\",\"status\":\"delivering\",\"status_name\":\"Đang giao hàng\",\"to_name\":\"Nguyen Van A\",\"to_phone\":\"0912345678\",\"to_address\":\"123 ABC Street\",\"log\":[{\"status\":\"picked\",\"updated_date\":\"2025-12-16 10:00:00\"},{\"status\":\"storing\",\"updated_date\":\"2025-12-16 12:00:00\"}]}}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Order code not found")
    })
    @GetMapping("/detail/{orderCode}")
    public ResponseEntity<?> detail(
        @Parameter(description = "GHN order code (from create shipment response)", required = true, example = "GHN123456")
        @PathVariable String orderCode,
        @Parameter(description = "Shop ID (optional)", example = "123")
        @RequestParam(name = "shopId", required = false) Long shopId
    ) {
        return ResponseEntity.ok(ghnService.getOrderDetail(orderCode, shopId));
    }

    @Operation(
        summary = "Create order with GHN (simplified)",
        description = "Convenience endpoint to create GHN shipment using simplified CreateOrderRequestDTO. Automatically converts to GHN format. **Address fields used: provinceId → to_district_id, wardCode → to_ward_code. typeAddress is NOT used.**"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully created order",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Order Response",
                    value = "{\"code\":200,\"message\":\"Success\",\"data\":{\"order_code\":\"GHN789012\",\"total_fee\":22000}}"
                )
            )
        )
    })
    @PostMapping("/order")
    public ResponseEntity<?> createOrderWithGhn(
        @Valid @RequestBody CreateOrderRequestDTO req,
        @Parameter(description = "Shop ID (optional)", example = "123")
        @RequestParam(name = "shopId", required = false) Long shopId
    ) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("to_name", req.getReceiverName());
        payload.put("to_phone", req.getReceiverPhone());
        try { payload.put("to_district_id", Integer.parseInt(req.getToDistrictId())); } catch (Exception ignored) {}
        if (req.getToWardCode() != null) payload.put("to_ward_code", req.getToWardCode());
        payload.put("to_address", req.getReceiverAddress());
        payload.put("weight", req.getWeightGrams());
        payload.put("client_order_code", "ORDER_" + (req.getOrderReference() != null ? req.getOrderReference() : "TEMP"));
        payload.put("required_note", "KHONGCHOXEMHANG");
        if (req.getCodAmount() != null) payload.put("cod_amount", req.getCodAmount().intValue());
        if (req.getItems() != null) {
            payload.put("items", req.getItems().stream().map(i -> {
                Map<String, Object> m = new HashMap<>();
                m.put("name", "Item-" + i.getVariantId());
                m.put("quantity", i.getQuantity());
                m.put("price", 0);
                return m;
            }).collect(Collectors.toList()));
        }

    Map<String, Object> resp = ghnService.createShippingOrder(payload, shopId);
        if (req.getOrderReference() != null) {
            Shipment s = new Shipment();
            try { s.setOrderId(Long.parseLong(req.getOrderReference())); } catch (NumberFormatException ignored) {}
            Object data = resp.get("data");
            if (data instanceof Map) {
                Object orderCode = ((Map<?, ?>) data).get("order_code");
                if (orderCode != null) s.setGhnOrderCode(String.valueOf(orderCode));
                Object totalFee = ((Map<?, ?>) data).get("total_fee");
                // Shipping fee now set on Order, not Shipment
            }
            s.setGhnPayload(ghnService.toJson(resp));
            s.setStatus("CREATED");
            shipmentRepository.save(s);
        }
        return ResponseEntity.ok(resp);
    }
}