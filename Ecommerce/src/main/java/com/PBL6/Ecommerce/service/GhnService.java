package com.PBL6.Ecommerce.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.PBL6.Ecommerce.domain.Shipment;
import com.PBL6.Ecommerce.repository.ShipmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.*;

@Service
public class GhnService {

    @Value("${ghn.api.url}")
    private String ghnApiUrl;

    private final RestTemplate restTemplate;
    private final ShopRepository shopRepository;
    private final ShipmentRepository shipmentRepository;
    private final ObjectMapper objectMapper;

    public GhnService(RestTemplate restTemplate, 
                      ShopRepository shopRepository,
                      ShipmentRepository shipmentRepository) {
        this.restTemplate = restTemplate;
        this.shopRepository = shopRepository;
        this.shipmentRepository = shipmentRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Chuẩn hóa payload cho GHN API
     * GHN yêu cầu ShopID CÙNG LÚC ở cả Header (ShopId) VÀ Body (shop_id)
     */
    private Map<String, Object> normalizePayload(Map<String, Object> payload, Shop shop) {
        Map<String, Object> normalized = new HashMap<>(payload);
        
        // ✅ Thêm shop_id vào body
        if (shop.getGhnShopId() != null && !shop.getGhnShopId().trim().isEmpty()) {
            try {
                Integer shopIdInt = Integer.parseInt(shop.getGhnShopId().trim());
                normalized.put("shop_id", shopIdInt);
            } catch (NumberFormatException e) {
                throw new RuntimeException("GHN Shop ID không hợp lệ: " + shop.getGhnShopId());
            }
        }
        
        // Xóa ShopID nếu có (legacy)
        normalized.remove("ShopID");
        
        // ✅ DEBUG: Log để kiểm tra required_note
        System.out.println("========== normalizePayload ==========");
        System.out.println("Input payload keys: " + payload.keySet());
        System.out.println("Input required_note: " + payload.get("required_note"));
        System.out.println("Shop GHN ID: " + shop.getGhnShopId());
        System.out.println("Output payload keys: " + normalized.keySet());
        System.out.println("Output required_note: " + normalized.get("required_note"));
        System.out.println("=====================================");
        
        return normalized;
    }

    /**
     * Lấy headers với token và ShopId từ shop
     */
    private HttpHeaders getGhnHeaders(Shop shop) {
        if (shop.getGhnToken() == null || shop.getGhnToken().trim().isEmpty()) {
            throw new RuntimeException("Shop chưa cấu hình GHN Token");
        }
        if (shop.getGhnShopId() == null || shop.getGhnShopId().trim().isEmpty()) {
            throw new RuntimeException("Shop chưa cấu hình GHN ShopId");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", shop.getGhnToken());
        headers.set("ShopId", shop.getGhnShopId());
        
        return headers;
    }

    /**
     * Lấy danh sách dịch vụ vận chuyển khả dụng
     */
    public List<Map<String,Object>> getAvailableServices(Map<String,Object> payload, Long shopId) {
        System.out.println("========== GHN SERVICE getAvailableServices ===========");
        System.out.println("Received payload: " + payload);
        System.out.println("ShopId: " + shopId);
        
        try {
            Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
            
            System.out.println("Found shop: " + shop.getName());
            System.out.println("Shop GHN Token: " + shop.getGhnToken());
            System.out.println("Shop GHN ShopId: " + shop.getGhnShopId());

            // MOCK LOGIC: If token is 'DUMMY_TOKEN', return mock data
            if ("DUMMY_TOKEN".equals(shop.getGhnToken())) {
                System.out.println("[MOCK] Returning standard delivery service for shopId=" + shopId);
                List<Map<String, Object>> mockServices = new ArrayList<>();
                Map<String, Object> standardService = new HashMap<>();
                standardService.put("service_id", 53320);
                standardService.put("short_name", "Giao hàng tiêu chuẩn");
                standardService.put("service_type_id", 1);
                standardService.put("service_name", "Giao hàng tiêu chuẩn");
                standardService.put("description", "Dịch vụ giao hàng tiêu chuẩn");
                mockServices.add(standardService);
                return mockServices;
            }

            HttpHeaders headers = getGhnHeaders(shop);

            // Chuẩn hóa payload
            Map<String, Object> normalizedPayload = normalizePayload(payload, shop);

            System.out.println("========== GHN AVAILABLE SERVICES REQUEST ==========");
            System.out.println("URL: " + ghnApiUrl + "/v2/shipping-order/available-services");
            System.out.println("Headers: " + headers);
            System.out.println("Body: " + normalizedPayload);
            System.out.println("===================================================");

            HttpEntity<Map<String,Object>> request = new HttpEntity<>(normalizedPayload, headers);

            String url = ghnApiUrl + "/v2/shipping-order/available-services";

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            System.out.println("========== GHN AVAILABLE SERVICES RESPONSE ==========");
            System.out.println("Status Code: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
            System.out.println("====================================================");

            Map<String,Object> body = response.getBody();
            System.out.println("GHN Response: " + body);

            if (body != null && body.get("data") instanceof List) {
                List<Map<String,Object>> allServices = (List<Map<String,Object>>) body.get("data");
                // Only return the first service as "Standard Delivery"
                List<Map<String,Object>> standardServices = new ArrayList<>();
                if (!allServices.isEmpty()) {
                    Map<String,Object> firstService = new HashMap<>(allServices.get(0));
                    firstService.put("short_name", "Giao hàng tiêu chuẩn");
                    firstService.put("service_name", "Giao hàng tiêu chuẩn");
                    firstService.put("description", "Dịch vụ giao hàng tiêu chuẩn");
                    standardServices.add(firstService);
                }
                System.out.println("Returning " + standardServices.size() + " standard delivery service to client");
                return standardServices;
            }
            System.out.println("No services found - returning empty list");
            return new ArrayList<>();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("❌ GHN Error Response: " + e.getResponseBodyAsString());
            throw new RuntimeException("Lỗi gọi GHN available-services: " + e.getMessage() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi gọi GHN available-services: " + e.getMessage());
        }
    }

    public Map<String,Object> calculateFee(Map<String,Object> payload, Long shopId) {
        try {
            Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
            
            // ✅ Weight-based service selection logic
            Integer totalWeight = (Integer) payload.getOrDefault("weight", 0);
            String packageType = totalWeight < 1000 ? "light" : "heavy"; // Under 1kg = light, over 1kg = heavy
            
            System.out.println("========== WEIGHT-BASED CALCULATION ==========");
            System.out.println("Total Weight: " + totalWeight + "g");
            System.out.println("Package Type: " + packageType);
            System.out.println("==============================================");
            
            // For DUMMY_TOKEN shops, return mock fee based on weight
            if ("DUMMY_TOKEN".equals(shop.getGhnToken())) {
                System.out.println("[MOCK] Calculating fee based on weight and location");
                Map<String, Object> mockResponse = new HashMap<>();
                Map<String, Object> data = new HashMap<>();
                
                // Base fee calculation based on weight and location
                int baseFee = packageType.equals("light") ? 25000 : 45000; // Light: 25k, Heavy: 45k
                
                // Location factor (can be enhanced with real province/district mapping)
                Integer toDistrictId = (Integer) payload.get("to_district_id");
                Integer fromDistrictId = (Integer) payload.get("from_district_id");
                double locationMultiplier = 1.0;
                
                if (toDistrictId != null && fromDistrictId != null) {
                    // Simple distance simulation - in reality this would be more complex
                    int distanceFactor = Math.abs(toDistrictId - fromDistrictId);
                    locationMultiplier = 1.0 + (distanceFactor > 100 ? 0.4 : distanceFactor > 50 ? 0.2 : 0.1);
                }
                
                int finalFee = (int) (baseFee * locationMultiplier);
                
                data.put("total", finalFee);
                data.put("service_fee", finalFee);
                data.put("insurance_fee", 0);
                data.put("cod_fee", 0);
                data.put("coupon_value", 0);
                data.put("package_type", packageType);
                data.put("weight_category", packageType.equals("light") ? "Hàng nhẹ" : "Hàng nặng");
                
                mockResponse.put("code", 200);
                mockResponse.put("message", "Success");
                mockResponse.put("data", data);
                
                System.out.println("Mock fee calculated: " + finalFee + " VND for " + packageType + " package");
                return mockResponse;
            }
            
            HttpHeaders headers = getGhnHeaders(shop);
            
            // ✅ Chuẩn hóa payload và thêm thông tin trọng lượng
            Map<String, Object> normalizedPayload = normalizePayload(payload, shop);
            
            System.out.println("========== GHN CALCULATE FEE REQUEST ==========");
            System.out.println("URL: " + ghnApiUrl + "/v2/shipping-order/fee");
            System.out.println("Payload: " + normalizedPayload);
            System.out.println("Package Type: " + packageType);
            System.out.println("==============================================");
            
            HttpEntity<Map<String,Object>> request = new HttpEntity<>(normalizedPayload, headers);
            
            String url = ghnApiUrl + "/v2/shipping-order/fee";
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            // Enhance response with weight information
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.get("data") instanceof Map) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                data.put("package_type", packageType);
                data.put("weight_category", packageType.equals("light") ? "Hàng nhẹ" : "Hàng nặng");
            }
            
            System.out.println("========== GHN CALCULATE FEE RESPONSE ==========");
            System.out.println("Response: " + responseBody);
            System.out.println("================================================");
            
            return responseBody;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("❌ GHN Error Response: " + e.getResponseBodyAsString());
            throw new RuntimeException("Lỗi tính phí: " + e.getMessage() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi tính phí: " + e.getMessage());
        }
    }

    public Map<String,Object> createShippingOrder(Map<String,Object> payload, Long shopId) {
        try {
            Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
            
            HttpHeaders headers = getGhnHeaders(shop);
            
            // ✅ Chuẩn hóa payload
            Map<String, Object> normalizedPayload = normalizePayload(payload, shop);
            
            // ✅ FORCE thêm required_note nếu thiếu
            if (!normalizedPayload.containsKey("required_note") || normalizedPayload.get("required_note") == null) {
                System.err.println("⚠️ WARNING: required_note is missing, setting default value");
                normalizedPayload.put("required_note", "KHONGCHOXEMHANG");
            }
            
            // ✅ Validate required_note value
            String requiredNote = String.valueOf(normalizedPayload.get("required_note"));
            List<String> validValues = Arrays.asList("CHOTHUHANG", "CHOXEMHANGKHONGTHU", "KHONGCHOXEMHANG");
            if (!validValues.contains(requiredNote)) {
                System.err.println("⚠️ WARNING: Invalid required_note value: " + requiredNote);
                normalizedPayload.put("required_note", "KHONGCHOXEMHANG");
            }
            
            // ✅ DEBUG: Log đầy đủ
            System.out.println("========== CREATE SHIPPING ORDER REQUEST ==========");
            System.out.println("URL: " + ghnApiUrl + "/v2/shipping-order/create");
            System.out.println("Headers: " + headers);
            System.out.println("Full Payload: " + normalizedPayload);
            System.out.println("Required Note: " + normalizedPayload.get("required_note"));
            
            // ✅ Log JSON body sẽ gửi
            try {
                String jsonBody = objectMapper.writeValueAsString(normalizedPayload);
                System.out.println("JSON Body sent to GHN:");
                System.out.println(jsonBody);
                
                // Verify JSON contains required_note
                if (!jsonBody.contains("required_note")) {
                    System.err.println("❌ CRITICAL: required_note NOT FOUND in JSON!");
                    throw new RuntimeException("Payload serialization error: required_note missing");
                }
            } catch (Exception e) {
                System.err.println("❌ Error serializing payload: " + e.getMessage());
            }
            System.out.println("===================================================");
            
            HttpEntity<Map<String,Object>> request = new HttpEntity<>(normalizedPayload, headers);
            
            String url = ghnApiUrl + "/v2/shipping-order/create";
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            System.out.println("========== CREATE SHIPPING ORDER RESPONSE ==========");
            System.out.println("Response: " + response.getBody());
            System.out.println("====================================================");
            
            return response.getBody();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("❌ GHN Error Response: " + e.getResponseBodyAsString());
            throw new RuntimeException("Lỗi tạo đơn: " + e.getMessage() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi tạo đơn: " + e.getMessage());
        }
    }

    /**
     * Get order detail from GHN by order code
     */
    public Map<String, Object> getOrderDetail(String orderCode, Long shopId) {
        try {
            Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
            
            HttpHeaders headers = getGhnHeaders(shop);
            
            Map<String, Object> body = new HashMap<>();
            body.put("order_code", orderCode);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            
            String url = ghnApiUrl + "/v2/shipping-order/detail";
            
            System.out.println("========== GET ORDER DETAIL REQUEST ==========");
            System.out.println("Order Code: " + orderCode);
            System.out.println("URL: " + url);
            System.out.println("==============================================");
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            System.out.println("========== GET ORDER DETAIL RESPONSE ==========");
            System.out.println("Response: " + response.getBody());
            System.out.println("===============================================");
            
            return response.getBody();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("❌ GHN Error Response: " + e.getResponseBodyAsString());
            throw new RuntimeException("Lỗi lấy chi tiết đơn: " + e.getMessage() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi lấy chi tiết đơn: " + e.getMessage());
        }
    }

    /**
     * Tạo đơn hàng GHN và lưu vào bảng shipments (async best-effort)
     */
    public Shipment createShippingOrderAsync(Long orderId, Map<String, Object> payload) {
        try {
            // Lấy shopId từ payload hoặc từ order
            Long shopId = null;
            if (payload.containsKey("shop_id")) {
                shopId = Long.valueOf(String.valueOf(payload.get("shop_id")));
            }
            
            // Gọi GHN API để tạo đơn
            Map<String, Object> ghnResponse = createShippingOrder(payload, shopId);
            
            // Parse response
            if (ghnResponse == null || ghnResponse.get("code") == null) {
                System.err.println("❌ GHN response is null or missing code");
                return null;
            }
            
            int code = Integer.parseInt(String.valueOf(ghnResponse.get("code")));
            if (code != 200) {
                System.err.println("❌ GHN create order failed with code: " + code);
                return null;
            }
            
            Shipment shipment = new Shipment();
            
            // Lấy thông tin từ GHN response
            Object data = ghnResponse.get("data");
            if (data instanceof Map) {
                Map<?, ?> dataMap = (Map<?, ?>) data;
                
                // Order code từ GHN
                Object orderCode = dataMap.get("order_code");
                if (orderCode != null) {
                    shipment.setGhnOrderCode(String.valueOf(orderCode));
                }
                
                // Shipping fee
                // Shipping fee now set on Order, not Shipment
                
                // Expected delivery time
                Object expectedDelivery = dataMap.get("expected_delivery_time");
                if (expectedDelivery != null) {
                    // Parse to LocalDateTime if needed, or store as string in another field
                }
            }
            
            // Lưu toàn bộ response từ GHN dưới dạng JSON
            try {
                shipment.setGhnPayload(objectMapper.writeValueAsString(ghnResponse));
            } catch (Exception e) {
                shipment.setGhnPayload(ghnResponse.toString());
            }
            
            // Set initial status
            shipment.setStatus("CREATED");
            
            // Lưu vào database
            Shipment saved = shipmentRepository.save(shipment);
            System.out.println("✅ Shipment saved successfully: " + saved.getId());
            
            return saved;
            
        } catch (Exception e) {
            System.err.println("❌ Error creating shipping order async: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Helper method để lấy String từ Map
     */
    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? String.valueOf(value) : null;
    }
    
    /**
     * Convert object sang JSON string
     */
    public String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj != null ? obj.toString() : null;
        }
    }
}