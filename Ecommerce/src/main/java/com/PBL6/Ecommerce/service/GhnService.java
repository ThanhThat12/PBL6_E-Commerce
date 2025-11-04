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
    // ensure required_note present (GHN requires this field)
    if (!body.containsKey("required_note")) {
        body.put("required_note", "KHONGCHOXEMHANG"); // hoặc giá trị phù hợp với dịch vụ của bạn
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

    @Async
    public void createShippingOrderAsync(Long orderId, Map<String,Object> payload) {
        Map<String,Object> resp;
        try {
            resp = createShippingOrder(payload);
        } catch (Exception ex) {
            // save failure info for later retry / troubleshooting
            Shipment fail = new Shipment();
            fail.setOrderId(orderId);
            fail.setStatus("GHN_ERROR");
            fail.setGhnPayload(toJson(Map.of("error", ex.getMessage(), "request", payload)));
            shipmentRepository.save(fail);
            return;
        }

        Shipment s = new Shipment();
        s.setOrderId(orderId);
        Object data = resp.get("data");
        if (data instanceof Map) {
            Object orderCode = ((Map<?,?>) data).get("order_code");
            if (orderCode != null) s.setGhnOrderCode(String.valueOf(orderCode));
        }
        s.setStatus("CREATED");
        s.setGhnPayload(toJson(resp));
        shipmentRepository.save(s);
    }

    public String toJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); }
        catch (JsonProcessingException e) { return "{}"; }
    }
}
// ...existing code...