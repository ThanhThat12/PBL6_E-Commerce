package com.PBL6.Ecommerce.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.PBL6.Ecommerce.domain.entity.shop.Shop;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.PBL6.Ecommerce.domain.entity.order.Shipment;
import com.PBL6.Ecommerce.repository.ShipmentRepository;
import com.PBL6.Ecommerce.domain.entity.user.Address;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.*;

@Service
public class GhnService {

    @Value("${ghn.api.url}")
    private String ghnApiUrl;

    @Value("${ghn.token}")
    private String ghnToken;

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
        
        return normalized;
    }

    /**
     * Lấy headers với token (từ application.properties) và ShopId (từ Shop entity)
     */
    private HttpHeaders getGhnHeaders(Shop shop) {
        // ✅ Token được quản lý tập trung trong application.properties
        if (ghnToken == null || ghnToken.trim().isEmpty()) {
            throw new RuntimeException("GHN Token chưa được cấu hình trong application.properties");
        }
        
        // ✅ ShopId được lưu trong database (ghn_shop_id)
        if (shop.getGhnShopId() == null || shop.getGhnShopId().trim().isEmpty()) {
            throw new RuntimeException("Shop chưa có GHN Shop ID (chưa được đăng ký)");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghnToken); // ✅ Dùng token từ application.properties
        headers.set("ShopId", shop.getGhnShopId()); // ✅ Dùng shop_id từ database
        
        return headers;
    }

    /**
     * Lấy danh sách dịch vụ vận chuyển khả dụng
     */
    public List<Map<String,Object>> getAvailableServices(Map<String,Object> payload, Long shopId) {
        try {
            Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

            HttpHeaders headers = getGhnHeaders(shop);

            // Chuẩn hóa payload
            Map<String, Object> normalizedPayload = normalizePayload(payload, shop);
            
            HttpEntity<Map<String,Object>> request = new HttpEntity<>(normalizedPayload, headers);
            String url = ghnApiUrl + "/v2/shipping-order/available-services";

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            Map<String,Object> body = response.getBody();

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
                return standardServices;
            }
            return new ArrayList<>();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
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

    /**
     * Create GHN shipping order
     * API: POST /v2/shipping-order/create
     * 
     * Required fields theo tài liệu GHN:
     * - payment_type_id: 1=Shop trả, 2=Buyer trả
     * - required_note: CHOTHUHANG | CHOXEMHANGKHONGTHU | KHONGCHOXEMHANG
     * - from_name, from_phone, from_address, from_ward_name, from_district_name, from_province_name
     * - to_name, to_phone, to_address, to_ward_code, to_district_id
     * - weight, length, width, height
     * - service_id, service_type_id (0 = auto-detect)
     * - items array
     */
    public Map<String,Object> createShippingOrder(Map<String,Object> payload, Long shopId) {
        try {
            Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
            
            HttpHeaders headers = getGhnHeaders(shop);
            
            // ✅ Chuẩn hóa payload
            Map<String, Object> normalizedPayload = normalizePayload(payload, shop);
            
            // ✅ Validate required fields theo tài liệu GHN
            validateGhnPayload(normalizedPayload);
            
            // Ensure required_note has valid value
            if (!normalizedPayload.containsKey("required_note") || normalizedPayload.get("required_note") == null) {
                normalizedPayload.put("required_note", "KHONGCHOXEMHANG");
            }
            
            // Validate required_note value
            String requiredNote = String.valueOf(normalizedPayload.get("required_note"));
            List<String> validValues = Arrays.asList("CHOTHUHANG", "CHOXEMHANGKHONGTHU", "KHONGCHOXEMHANG");
            if (!validValues.contains(requiredNote)) {
                normalizedPayload.put("required_note", "KHONGCHOXEMHANG");
            }
            
            HttpEntity<Map<String,Object>> request = new HttpEntity<>(normalizedPayload, headers);
            
            String url = ghnApiUrl + "/v2/shipping-order/create";
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            System.out.println("========== CREATE SHIPPING ORDER RESPONSE ==========");
            System.out.println("HTTP Status: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
            System.out.println("====================================================");
            
            Map<String, Object> responseBody = response.getBody();
            
            // ✅ Kiểm tra GHN response code
            if (responseBody != null) {
                Object codeObj = responseBody.get("code");
                if (codeObj != null) {
                    int ghnCode = Integer.parseInt(String.valueOf(codeObj));
                    if (ghnCode != 200) {
                        String message = String.valueOf(responseBody.get("message"));
                        System.err.println("❌ GHN API Error: code=" + ghnCode + ", message=" + message);
                        throw new RuntimeException("GHN API Error: " + message + " (code: " + ghnCode + ")");
                    }
                }
            }
            
            return responseBody;
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

    /**
     * Register new shop with GHN and return shop_id
     * API: POST https://online-gateway.ghn.vn/shiip/public-api/v2/shop/register
     * 
     * @param shopName - Name of the shop
     * @param shopPhone - Phone number of the shop
     * @param address - Full shop address (store address from Address entity)
     * @return GHN shop_id as String
     * @throws RuntimeException if registration fails
     */
    public String registerGhnShop(String shopName, String shopPhone, Address address) {
        try {
            System.out.println("========== GHN SHOP REGISTRATION ==========");
            System.out.println("Shop Name: " + shopName);
            System.out.println("Shop Phone: " + shopPhone);
            System.out.println("Address: " + (address != null ? address.getFullAddress() : "null"));
            
            // Validate required fields
            if (shopName == null || shopName.trim().isEmpty()) {
                throw new RuntimeException("Shop name is required for GHN registration");
            }
            if (shopPhone == null || shopPhone.trim().isEmpty()) {
                throw new RuntimeException("Shop phone is required for GHN registration");
            }
            if (address == null) {
                throw new RuntimeException("Shop address is required for GHN registration");
            }
            if (address.getDistrictId() == null) {
                throw new RuntimeException("District ID is required for GHN registration");
            }
            if (address.getWardCode() == null || address.getWardCode().trim().isEmpty()) {
                throw new RuntimeException("Ward code is required for GHN registration");
            }

            // Prepare headers with GHN token
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("district_id", address.getDistrictId());
            requestBody.put("ward_code", address.getWardCode());
            requestBody.put("name", shopName.trim());
            requestBody.put("phone", shopPhone.trim());
            requestBody.put("address", address.getFullAddress());

            System.out.println("Request Body: " + requestBody);
            System.out.println("GHN Token: " + ghnToken.substring(0, 10) + "...");

            // Make API call
            String url = ghnApiUrl + "/v2/shop/register";
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            System.out.println("========== GHN SHOP REGISTRATION RESPONSE ==========");
            System.out.println("Status Code: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
            System.out.println("===================================================");

            // Parse response
            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("GHN registration failed: Empty response");
            }

            Integer code = (Integer) responseBody.get("code");
            if (code == null || code != 200) {
                String message = (String) responseBody.get("message");
                throw new RuntimeException("GHN registration failed: " + message);
            }

            // Extract shop_id from data
            Object dataObj = responseBody.get("data");
            if (dataObj instanceof Map) {
                Map<?, ?> data = (Map<?, ?>) dataObj;
                Object shopIdObj = data.get("shop_id");
                
                if (shopIdObj != null) {
                    String shopId = String.valueOf(shopIdObj);
                    System.out.println("✅ GHN Shop registered successfully! Shop ID: " + shopId);
                    return shopId;
                }
            }

            throw new RuntimeException("GHN registration failed: shop_id not found in response");

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("❌ GHN Registration Error: " + e.getResponseBodyAsString());
            throw new RuntimeException("Lỗi đăng ký shop GHN: " + e.getMessage() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("❌ GHN Registration Exception: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi đăng ký shop GHN: " + e.getMessage());
        }
    }
    
    /**
     * Validate GHN payload trước khi gửi API
     * Kiểm tra các trường bắt buộc theo tài liệu GHN
     */
    private void validateGhnPayload(Map<String, Object> payload) {
        // Required fields for shipping order creation
        String[] requiredFields = {
            "to_name", "to_phone", "to_address", "to_ward_code", "to_district_id",
            "from_name", "from_phone", "from_address",
            "weight", "service_type_id"
        };
        
        for (String field : requiredFields) {
            if (!payload.containsKey(field) || payload.get(field) == null) {
                throw new RuntimeException("Missing required field: " + field);
            }
            
            // Check empty string
            if (payload.get(field) instanceof String) {
                String value = (String) payload.get(field);
                if (value.trim().isEmpty()) {
                    throw new RuntimeException("Empty value for required field: " + field);
                }
            }
        }
        
        // Validate numeric fields
        Integer weight = (Integer) payload.get("weight");
        if (weight != null && weight < 1) {
            throw new RuntimeException("Weight must be at least 1 gram");
        }
        
        Integer toDistrictId = (Integer) payload.get("to_district_id");
        if (toDistrictId != null && toDistrictId <= 0) {
            throw new RuntimeException("Invalid to_district_id: " + toDistrictId);
        }
        
        // Validate items array if present
        if (payload.containsKey("items") && payload.get("items") != null) {
            Object itemsObj = payload.get("items");
            if (itemsObj instanceof List) {
                List<?> items = (List<?>) itemsObj;
                if (items.isEmpty()) {
                    System.err.println("⚠️ WARNING: items array is empty");
                }
            }
        }
        
        System.out.println("✅ GHN payload validation passed");
    }
}