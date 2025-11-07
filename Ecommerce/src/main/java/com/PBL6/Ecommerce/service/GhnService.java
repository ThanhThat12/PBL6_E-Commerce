// ...existing code...
package com.PBL6.Ecommerce.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.PBL6.Ecommerce.domain.Shipment;
import com.PBL6.Ecommerce.repository.ShipmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.HashMap;

@Service
public class GhnService {

    private final RestTemplate restTemplate;
    private final ShipmentRepository shipmentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ghn.api.url:https://dev-online-gateway.ghn.vn/shiip/public-api}")
    private String ghnApiUrl;

    @Value("${ghn.token:}")
    private String ghnToken;

    @Value("${ghn.shop-id:}")
    private String ghnShopId;
// thêm 2 config tùy chọn (đặt vào application.properties nếu muốn)
    @Value("${ghn.from-district-id:}")
    private String ghnFromDistrictId;

    @Value("${ghn.from-ward-code:}")
    private String ghnFromWardCode;
    
    public GhnService(RestTemplate restTemplate, ShipmentRepository shipmentRepository) {
        this.restTemplate = restTemplate;
        this.shipmentRepository = shipmentRepository;
    }

    private HttpHeaders baseHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", ghnToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public Map<String,Object> calculateFee(Map<String,Object> payload) {
        String url = ghnApiUrl + "/v2/shipping-order/fee";
        HttpEntity<Map<String,Object>> req = new HttpEntity<>(payload, baseHeaders());
        ResponseEntity<Map> resp = restTemplate.postForEntity(url, req, Map.class);
        if (!resp.getStatusCode().is2xxSuccessful()) throw new RuntimeException("GHN fee failed: " + resp.getStatusCode());
        return resp.getBody();
    }

    public Map<String,Object> createShippingOrder(Map<String,Object> payload) {
    String url = ghnApiUrl + "/v2/shipping-order/create";
    // copy payload to mutable map to avoid UnsupportedOperationException when incoming map is immutable
    Map<String,Object> body = new HashMap<>(payload == null ? Map.of() : payload);
    if (!body.containsKey("shop_id") && ghnShopId != null && !ghnShopId.isBlank()) {
        try { body.put("shop_id", Long.parseLong(ghnShopId)); }
        catch (NumberFormatException ex) { body.put("shop_id", ghnShopId); }
    }
    // Add sender's district and ward code (required by GHN)
    if (!body.containsKey("from_district_id") && ghnFromDistrictId != null && !ghnFromDistrictId.isBlank()) {
        try { body.put("from_district_id", Integer.parseInt(ghnFromDistrictId)); }
        catch (NumberFormatException ex) { body.put("from_district_id", ghnFromDistrictId); }
    }
    if (!body.containsKey("from_ward_code") && ghnFromWardCode != null && !ghnFromWardCode.isBlank()) {
        body.put("from_ward_code", ghnFromWardCode);
    }
    // ensure required_note present (GHN requires this field)
    if (!body.containsKey("required_note")) {
        body.put("required_note", "KHONGCHOXEMHANG"); // hoặc giá trị phù hợp với dịch vụ của bạn
    }
    // Add default service_id and service_type_id for standard delivery
    if (!body.containsKey("service_id")) {
        body.put("service_id", 0); // 0 = standard service, GHN will auto-select
    }
    if (!body.containsKey("service_type_id")) {
        body.put("service_type_id", 2); // 2 = same day delivery
    }
    // ensure payment_type_id present and valid
        if (!body.containsKey("payment_type_id")) {
            int cod = 0;
            Object codObj = body.get("cod_amount");
            if (codObj instanceof Number) {
                cod = ((Number) codObj).intValue();
            } else if (codObj instanceof String) {
                try { cod = Integer.parseInt((String) codObj); } catch (Exception ignored) {}
            }
            // mặc định: nếu có COD (>0) thì đặt payment_type_id = 2 (thu hộ), ngược lại = 1 (người gửi trả)
            body.put("payment_type_id", cod > 0 ? 2 : 1);
        }

    HttpEntity<Map<String,Object>> req = new HttpEntity<>(body, baseHeaders());
    ResponseEntity<Map> resp = restTemplate.postForEntity(url, req, Map.class);
    if (!resp.getStatusCode().is2xxSuccessful()) throw new RuntimeException("GHN create shipment failed: " + resp.getStatusCode());
    return resp.getBody();
}

    public Map<String,Object> getOrderDetail(String orderCode) {
        String url = ghnApiUrl + "/v2/shipping-order/detail?order_code=" + orderCode;
        HttpEntity<Void> req = new HttpEntity<>(baseHeaders());
        ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.GET, req, Map.class);
        if (!resp.getStatusCode().is2xxSuccessful()) throw new RuntimeException("GHN detail failed: " + resp.getStatusCode());
        return resp.getBody();
    }

    public Shipment createShippingOrderAsync(Long orderId, Map<String,Object> payload) {
        Map<String,Object> resp;
        try {
            resp = createShippingOrder(payload);
        } catch (Exception ex) {
            // save failure info for later retry / troubleshooting
            Shipment fail = new Shipment();
            fail.setOrderId(orderId);
            fail.setReceiverName((String) payload.get("to_name"));
            fail.setReceiverPhone((String) payload.get("to_phone"));
            fail.setReceiverAddress((String) payload.get("to_address"));
            fail.setProvince((String) payload.get("province"));
            fail.setDistrict((String) payload.get("district"));
            fail.setWard((String) payload.get("ward"));
            fail.setStatus("GHN_ERROR");
            fail.setGhnPayload(toJson(Map.of("error", ex.getMessage(), "request", payload)));
            return shipmentRepository.save(fail);
        }

        Shipment s = new Shipment();
        s.setOrderId(orderId);
        s.setReceiverName((String) payload.get("to_name"));
        s.setReceiverPhone((String) payload.get("to_phone"));
        s.setReceiverAddress((String) payload.get("to_address"));
        s.setProvince((String) payload.get("province"));
        s.setDistrict((String) payload.get("district"));
        s.setWard((String) payload.get("ward"));
        
        // Use shipping fee from request payload (calculated by frontend)
        Object payloadShippingFee = payload.get("shipping_fee");
        if (payloadShippingFee != null) {
            if (payloadShippingFee instanceof java.math.BigDecimal) {
                s.setShippingFee((java.math.BigDecimal) payloadShippingFee);
            } else if (payloadShippingFee instanceof Number) {
                s.setShippingFee(java.math.BigDecimal.valueOf(((Number) payloadShippingFee).doubleValue()));
            }
        }
        
        // Generate unique GHN order code using timestamp
        String customOrderCode = "GHN" + System.currentTimeMillis();
        s.setGhnOrderCode(customOrderCode);
        
        // Extract data from GHN response
        Object data = resp.get("data");
        if (data instanceof Map) {
            Map<String,Object> dataMap = (Map<String,Object>) data;
            
            // Only use GHN shipping fee if not already set from request payload
            if (s.getShippingFee() == null) {
                Object totalFee = dataMap.get("total_fee");
                if (totalFee instanceof Number) {
                    s.setShippingFee(java.math.BigDecimal.valueOf(((Number) totalFee).doubleValue()));
                }
            }
            
            // Set expected delivery time
            Object expectedTime = dataMap.get("expected_delivery_time");
            if (expectedTime instanceof String) {
                try {
                    s.setExpectedDelivery(java.time.LocalDateTime.parse((String) expectedTime, 
                        java.time.format.DateTimeFormatter.ISO_DATE_TIME));
                } catch (Exception ignored) {}
            }
            
            // Set service type from trans_type
            Object transType = dataMap.get("trans_type");
            if (transType instanceof String) {
                s.setServiceType((String) transType);
            }
        }
        
        s.setStatus("CREATED");
        s.setGhnPayload(toJson(resp));
        return shipmentRepository.save(s);
    }

    public String toJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); }
        catch (JsonProcessingException e) { return "{}"; }
    }
}
// ...existing code...