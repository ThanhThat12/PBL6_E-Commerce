package com.PBL6.Ecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.*;
import com.PBL6.Ecommerce.constant.TypeAddress;
import com.PBL6.Ecommerce.service.GhnService;
import com.PBL6.Ecommerce.repository.*;

import java.util.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/seller/orders")
public class SellerOrderController {
    
    private final com.PBL6.Ecommerce.repository.OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final AddressRepository addressRepository;
    private final GhnService ghnService;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;

    public SellerOrderController(
            com.PBL6.Ecommerce.repository.OrderRepository orderRepository,
            ShopRepository shopRepository,
            AddressRepository addressRepository,
            GhnService ghnService,
            ShipmentRepository shipmentRepository,
            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.shopRepository = shopRepository;
        this.addressRepository = addressRepository;
        this.ghnService = ghnService;
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Seller xác nhận đơn hàng và tạo GHN shipment
     * POST /api/seller/orders/{orderId}/confirm-and-ship
     */
    @PostMapping("/{orderId}/confirm-and-ship")
    @PreAuthorize("hasRole('SELLER')")
    @Transactional
    public ResponseEntity<ResponseDTO<Map<String,Object>>> confirmAndShip(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            // Lấy email từ token
            String email = jwt.getClaimAsString("email");
            if (email == null) {
                throw new RuntimeException("Token không có email");
            }

            // Lấy User (seller)
            User seller = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Lấy order
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

            // Kiểm tra quyền
            if (!order.getShop().getOwner().getId().equals(seller.getId())) {
                throw new RuntimeException("Bạn không có quyền xác nhận đơn hàng này");
            }

            // Kiểm tra trạng thái
            if (order.getStatus() != Order.OrderStatus.PENDING) {
                throw new RuntimeException("Đơn hàng không ở trạng thái chờ xác nhận");
            }

            // Parse GHN info từ notes
            Map<String, Object> ghnInfo = new HashMap<>();
            try {
                ghnInfo = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(order.getNotes(), Map.class);
            } catch (Exception e) {
                throw new RuntimeException("Không tìm thấy thông tin GHN trong đơn hàng");
            }

            Integer serviceId = (Integer) ghnInfo.get("serviceId");
            Integer serviceTypeId = (Integer) ghnInfo.get("serviceTypeId");
            Long addressId = Long.valueOf(ghnInfo.get("addressId").toString());
            String note = (String) ghnInfo.get("note");

            if (serviceId == null) {
                throw new RuntimeException("Thiếu thông tin dịch vụ GHN");
            }

            // Lấy địa chỉ buyer
            Address buyerAddress = addressRepository.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));

            // Lấy địa chỉ shop
            Shop shop = order.getShop();
            Address shopAddress = addressRepository.findByUserAndTypeAddress(
                    shop.getOwner(), TypeAddress.STORE)
                    .stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ STORE của shop"));

            // ========== CHUẨN BỊ PAYLOAD CHO GHN ==========
            User buyer = order.getUser();
            
            int totalWeight = 0;
            int maxLength = 0, maxWidth = 0, maxHeight = 0;
            int codAmount = 0;
            List<Map<String, Object>> items = new ArrayList<>();

            for (var orderItem : order.getOrderItems()) {
                var product = orderItem.getVariant().getProduct();
                var variant = orderItem.getVariant();
                int quantity = orderItem.getQuantity();

                // Weight
                Integer weight = product.getWeightGrams();
                if (weight != null) {
                    totalWeight += weight * quantity;
                }

                // Dimensions
                if (product.getLengthCm() != null) {
                    maxLength = Math.max(maxLength, product.getLengthCm());
                }
                if (product.getWidthCm() != null) {
                    maxWidth = Math.max(maxWidth, product.getWidthCm());
                }
                if (product.getHeightCm() != null) {
                    maxHeight = Math.max(maxHeight, product.getHeightCm());
                }

                // COD amount
                if ("COD".equalsIgnoreCase(order.getMethod())) {
                    BigDecimal price = variant.getPrice();
                    if (price != null) {
                        codAmount += price.intValue() * quantity;
                    }
                }

                // Items array
                Map<String, Object> item = new HashMap<>();
                item.put("name", product.getName());
                item.put("code", variant.getSku() != null ? variant.getSku() : "");
                item.put("quantity", quantity);
                item.put("price", variant.getPrice() != null ? variant.getPrice().intValue() : 0);
                item.put("length", product.getLengthCm() != null ? product.getLengthCm() : 12);
                item.put("width", product.getWidthCm() != null ? product.getWidthCm() : 12);
                item.put("height", product.getHeightCm() != null ? product.getHeightCm() : 12);
                item.put("weight", weight != null ? weight : 200);

                Map<String, String> category = new HashMap<>();
                category.put("level1", product.getCategory() != null ?
                        product.getCategory().getName() : "Khác");
                item.put("category", category);

                items.add(item);
            }

            // Default values
            if (totalWeight == 0) totalWeight = 200;
            if (maxLength == 0) maxLength = 20;
            if (maxWidth == 0) maxWidth = 20;
            if (maxHeight == 0) maxHeight = 10;

            // ========== TẠO PAYLOAD CHO GHN ==========
            Map<String, Object> payload = new HashMap<>();

            // Thông tin người gửi (shop)
            payload.put("from_name", shopAddress.getContactName() != null ?
                    shopAddress.getContactName() : shop.getName());
            payload.put("from_phone", shopAddress.getContactPhone() != null ?
                    shopAddress.getContactPhone() : shop.getOwner().getPhoneNumber());
            payload.put("from_address", shopAddress.getFullAddress());
            payload.put("from_ward_name", shopAddress.getWardName());
            payload.put("from_district_name", shopAddress.getDistrictName());
            payload.put("from_province_name", shopAddress.getProvinceName());

            // Địa chỉ trả hàng
            payload.put("return_phone", shopAddress.getContactPhone() != null ?
                    shopAddress.getContactPhone() : shop.getOwner().getPhoneNumber());
            payload.put("return_address", shopAddress.getFullAddress());
            payload.put("return_district_id", null);
            payload.put("return_ward_code", "");

            // Thông tin người nhận (buyer)
            payload.put("to_name", buyerAddress.getContactName() != null ?
                    buyerAddress.getContactName() : buyer.getFullName());
            payload.put("to_phone", buyerAddress.getContactPhone() != null ?
                    buyerAddress.getContactPhone() : buyer.getPhoneNumber());
            payload.put("to_address", buyerAddress.getFullAddress());
            payload.put("to_ward_code", buyerAddress.getWardCode());
            payload.put("to_district_id", buyerAddress.getDistrictId());

            // Thông tin đơn hàng
            payload.put("weight", totalWeight);
            payload.put("length", maxLength);
            payload.put("width", maxWidth);
            payload.put("height", maxHeight);

            payload.put("service_id", serviceId);
            payload.put("service_type_id", serviceTypeId);

            payload.put("payment_type_id", 2); // Buyer trả ship
            payload.put("required_note", "KHONGCHOXEMHANG");
            payload.put("cod_amount", codAmount);
            payload.put("insurance_value", codAmount > 0 ? (int)(codAmount * 0.1) : 0);
            payload.put("items", items);

            payload.put("client_order_code", "ORD-" + order.getId());
            payload.put("note", note != null ? note : "");
            payload.put("content", "Đơn hàng từ " + shop.getName());
            payload.put("coupon", null);
            payload.put("pick_shift", new int[]{2});
            payload.put("pick_station_id", shopAddress.getDistrictId());
            payload.put("deliver_station_id", null);

            System.out.println("========== GHN PAYLOAD ==========");
            System.out.println("Order ID: " + order.getId());
            System.out.println("From: " + payload.get("from_name"));
            System.out.println("To: " + payload.get("to_name"));
            System.out.println("COD: " + codAmount);
            System.out.println("================================");

            // ========== GỌI GHN API TẠO VẬN ĐƠN ==========
            Map<String,Object> ghnResponse = ghnService.createShippingOrder(payload, shop.getId());

            // ========== TẠO SHIPMENT TRONG DB ==========
            Shipment shipment = new Shipment();
            shipment.setReceiverName(buyerAddress.getContactName());
            shipment.setReceiverPhone(buyerAddress.getContactPhone());
            shipment.setReceiverAddress(buyerAddress.getFullAddress());
            shipment.setProvince(buyerAddress.getProvinceName());
            shipment.setDistrict(buyerAddress.getDistrictName());
            shipment.setWard(buyerAddress.getWardName());
            shipment.setStatus("READY_TO_PICK");

            // Lấy thông tin từ GHN response
            if (ghnResponse != null && ghnResponse.get("data") instanceof Map) {
                Map<?, ?> data = (Map<?, ?>) ghnResponse.get("data");
                
                Object orderCode = data.get("order_code");
                if (orderCode != null) {
                    shipment.setGhnOrderCode(String.valueOf(orderCode));
                }

                Object totalFee = data.get("total_fee");
                if (totalFee instanceof Number) {
                    shipment.setShippingFee(BigDecimal.valueOf(((Number) totalFee).doubleValue()));
                }

                Object expectedDeliveryTime = data.get("expected_delivery_time");
                if (expectedDeliveryTime != null) {
                    shipment.setExpectedDeliveryTime(String.valueOf(expectedDeliveryTime));
                }
            }

            // Lưu toàn bộ response
            try {
                shipment.setGhnPayload(new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(ghnResponse));
            } catch (Exception e) {
                shipment.setGhnPayload(ghnResponse != null ? ghnResponse.toString() : "");
            }

            shipment = shipmentRepository.save(shipment);

            // ========== CẬP NHẬT ORDER ==========
            order.setStatus(Order.OrderStatus.PROCESSING);
            order.setShipment(shipment);
            orderRepository.save(order);

            // ========== TRẢ VỀ KẾT QUẢ ==========
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.getId());
            response.put("shipmentId", shipment.getId());
            response.put("ghnOrderCode", shipment.getGhnOrderCode());
            response.put("status", order.getStatus().name());
            response.put("shippingFee", shipment.getShippingFee());

            return ResponseDTO.ok(response, "Xác nhận đơn hàng và tạo vận đơn thành công");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(400, e.getMessage(), "Lỗi xác nhận đơn hàng", null));
        }
    }
}
