package com.PBL6.Ecommerce.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.PBL6.Ecommerce.domain.Address;
import com.PBL6.Ecommerce.domain.Shipment;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.repository.ShipmentRepository;
import com.PBL6.Ecommerce.repository.ShopRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Simplified, robust GHN integration service.
 * - Populates from_* from shop pickup address when possible
 * - Adds PascalCase / snake_case field variants required by GHN
 * - Wraps request body under "myRequest" for endpoints that expect it
 * - Uses per-shop GHN token/shop-id header if available; falls back to global config
 */
@Service
public class GhnService {

    private final RestTemplate restTemplate;
    private final ShipmentRepository shipmentRepository;
    private final ShopRepository shopRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(GhnService.class);

    @Value("${ghn.api.url}")
    private String ghnApiUrl;

    @Value("${ghn.token:}")
    private String ghnToken;

    @Value("${ghn.shop-id:}")
    private String ghnShopId;

    @Value("${ghn.from-district-id:}")
    private String ghnFromDistrictId;

    @Value("${ghn.from-ward-code:}")
    private String ghnFromWardCode;

    @Value("${ghn.default-service-id:}")
    private String ghnDefaultServiceId;

    @Value("${ghn.default-service-type-id:}")
    private String ghnDefaultServiceTypeId;

    public GhnService(RestTemplate restTemplate, ShipmentRepository shipmentRepository, ShopRepository shopRepository) {
        this.restTemplate = restTemplate;
        this.shipmentRepository = shipmentRepository;
        this.shopRepository = shopRepository;
    }

    private HttpHeaders baseHeaders() {
        return baseHeaders(null);
    }

    private HttpHeaders baseHeaders(Long providedShopId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String tokenToUse = null;
        String shopIdHeader = null;

        if (providedShopId != null) {
            try {
                Optional<Shop> os = shopRepository.findById(providedShopId);
                if (os.isPresent()) {
                    Shop s = os.get();
                    if (s.getGhnToken() != null && !s.getGhnToken().isBlank()) tokenToUse = s.getGhnToken();
                    if (s.getGhnShopId() != null && !s.getGhnShopId().isBlank()) shopIdHeader = s.getGhnShopId();
                    else shopIdHeader = String.valueOf(providedShopId);
                } else {
                    shopIdHeader = String.valueOf(providedShopId);
                }
            } catch (Exception ignored) {}
        }

        if (tokenToUse == null || tokenToUse.isBlank()) tokenToUse = (ghnToken == null) ? "" : ghnToken;
        if (tokenToUse != null && !tokenToUse.isBlank()) headers.set("Token", tokenToUse);

        if (shopIdHeader != null && !shopIdHeader.isBlank()) headers.set("ShopId", shopIdHeader);
        else if (ghnShopId != null && !ghnShopId.isBlank()) headers.set("ShopId", ghnShopId);

        // log masked token/shop for diagnostics
        try {
            if (logger.isInfoEnabled()) {
                String masked = "(none)";
                if (tokenToUse != null && !tokenToUse.isBlank()) {
                    String t = tokenToUse;
                    masked = t.length() > 6 ? (t.substring(0,3) + "..." + t.substring(t.length()-3)) : "***";
                }
                String shopHdr = headers.getFirst("ShopId") != null ? headers.getFirst("ShopId") : "(none)";
                logger.info("Using GHN token={} ShopIdHeader={}", masked, shopHdr);
            }
        } catch (Exception ignored) {}

        return headers;
    }

    private void logHeaders(HttpHeaders headers) {
        if (!logger.isDebugEnabled()) return;
        try {
            HttpHeaders copy = new HttpHeaders();
            copy.putAll(headers);
            if (copy.containsKey("Token")) {
                List<String> toks = copy.get("Token");
                if (toks != null && !toks.isEmpty()) {
                    String t = toks.get(0);
                    String masked = t.length() > 6 ? (t.substring(0,3) + "..." + t.substring(t.length()-3)) : "***";
                    copy.put("Token", List.of(masked));
                }
            }
            logger.debug("GHN outgoing headers: {}", copy);
        } catch (Exception ignored) {}
    }

    /**
     * Populate from_district_id / from_ward_code from shop pickup address when available.
     */
    private void populateFromByShop(Map<String, Object> body, Long shopId) {
        if (shopId == null) return;
        try {
            Optional<Shop> os = shopRepository.findById(shopId);
            if (os.isEmpty()) return;
            Shop s = os.get();
            if (s.getPickupAddress() != null) {
                Address pa = s.getPickupAddress();
                if (!body.containsKey("from_district_id") && pa.getDistrictId() != null)
                    body.put("from_district_id", pa.getDistrictId());
                if (!body.containsKey("from_ward_code") && pa.getWardCode() != null)
                    body.put("from_ward_code", pa.getWardCode());
            }
            // service defaults from shop
            try {
                Integer sid = s.getGhnServiceId();
                Integer stid = s.getGhnServiceTypeId();
                if (sid != null && !body.containsKey("service_id")) body.put("service_id", sid);
                if (stid != null && !body.containsKey("service_type_id")) body.put("service_type_id", stid);
            } catch (Exception ignored) {}
        } catch (Exception ignored) {}
    }

    /**
     * Add GHN field variants (PascalCase + snake_case) and validate minimal required fields.
     * Throws IllegalArgumentException when required keys missing so callers can handle.
     */
    private void ensureGhnFieldVariants(Map<String, Object> body, Long shopId) {
        if (body == null) return;

        // helpers
        java.util.function.Function<Object, Integer> toInt = (o) -> {
            if (o == null) return null;
            if (o instanceof Number) return ((Number) o).intValue();
            try { return Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return null; }
        };
        java.util.function.Function<Object, String> toStr = (o) -> o == null ? null : String.valueOf(o);

        Integer canonicalFromDistrict = toInt.apply(body.get("from_district_id"));
        if (canonicalFromDistrict == null) canonicalFromDistrict = toInt.apply(body.get("FromDistrictID"));

        Integer canonicalToDistrict = toInt.apply(body.get("to_district_id"));
        if (canonicalToDistrict == null) canonicalToDistrict = toInt.apply(body.get("ToDistrictID"));

        String canonicalFromWard = toStr.apply(body.get("from_ward_code"));
        if (canonicalFromWard == null) canonicalFromWard = toStr.apply(body.get("FromWardCode"));

        String canonicalToWard = toStr.apply(body.get("to_ward_code"));
        if (canonicalToWard == null) canonicalToWard = toStr.apply(body.get("ToWardCode"));

        Integer canonicalWeight = toInt.apply(body.get("weight"));
        if (canonicalWeight == null) canonicalWeight = toInt.apply(body.get("Weight"));

        // try shop pickup if missing
        if ((canonicalFromDistrict == null || canonicalFromDistrict == 0 || canonicalFromWard == null) && shopId != null) {
            try {
                Optional<Shop> os = shopRepository.findById(shopId);
                if (os.isPresent()) {
                    Shop s = os.get();
                    if (s.getPickupAddress() != null) {
                        Address pa = s.getPickupAddress();
                        if (canonicalFromDistrict == null && pa.getDistrictId() != null) canonicalFromDistrict = pa.getDistrictId();
                        if ((canonicalFromWard == null || canonicalFromWard.isBlank()) && pa.getWardCode() != null) canonicalFromWard = pa.getWardCode();
                    }
                }
            } catch (Exception ignored) {}
        }

        // fallback to global config
        if (canonicalFromDistrict == null && ghnFromDistrictId != null && !ghnFromDistrictId.isBlank()) {
            try { canonicalFromDistrict = Integer.parseInt(ghnFromDistrictId); } catch (Exception ignored) {}
        }
        if (canonicalFromWard == null && ghnFromWardCode != null && !ghnFromWardCode.isBlank()) {
            canonicalFromWard = ghnFromWardCode;
        }

        // canonical ShopID value (string)
        String canonicalShopIdStr = null;
        Object rawShopId = body.get("ShopID");
        if (rawShopId == null) rawShopId = body.get("shop_id");
        if (rawShopId != null) canonicalShopIdStr = String.valueOf(rawShopId);

        // if caller provided a numeric shopId parameter (not in body) prefer it
        if ((canonicalShopIdStr == null || canonicalShopIdStr.isBlank()) && shopId != null) {
            try {
                Optional<Shop> os = shopRepository.findById(shopId);
                if (os.isPresent() && os.get().getGhnShopId() != null && !os.get().getGhnShopId().isBlank()) canonicalShopIdStr = os.get().getGhnShopId();
                else canonicalShopIdStr = String.valueOf(shopId);
            } catch (Exception ignored) { canonicalShopIdStr = String.valueOf(shopId); }
        }
        if ((canonicalShopIdStr == null || canonicalShopIdStr.isBlank()) && ghnShopId != null && !ghnShopId.isBlank()) canonicalShopIdStr = ghnShopId;

        // remove conflicting keys then re-add canonical ones
        String[] removeKeys = new String[]{"from_district_id","FromDistrictID","to_district_id","ToDistrictID",
                "from_ward_code","FromWardCode","to_ward_code","ToWardCode","weight","Weight","ShopID","shop_id"};
        for (String k : removeKeys) body.remove(k);

        if (canonicalFromDistrict != null) {
            body.put("from_district_id", canonicalFromDistrict);
            body.put("FromDistrictID", canonicalFromDistrict);
        }
        if (canonicalToDistrict != null) {
            body.put("to_district_id", canonicalToDistrict);
            body.put("ToDistrictID", canonicalToDistrict);
        }
        if (canonicalFromWard != null) {
            body.put("from_ward_code", canonicalFromWard);
            body.put("FromWardCode", canonicalFromWard);
        }
        if (canonicalToWard != null) {
            body.put("to_ward_code", canonicalToWard);
            body.put("ToWardCode", canonicalToWard);
        }
        if (canonicalWeight != null) {
            body.put("weight", canonicalWeight);
            body.put("Weight", canonicalWeight);
        }
        if (canonicalShopIdStr != null) {
            body.put("ShopID", canonicalShopIdStr);
            try {
                Integer sid = Integer.parseInt(canonicalShopIdStr);
                body.put("shop_id", sid);
            } catch (Exception ex) {
                body.put("shop_id", canonicalShopIdStr);
            }
        }

        if (body.containsKey("service_id") && !body.containsKey("ServiceID")) body.put("ServiceID", body.get("service_id"));
        if (body.containsKey("service_type_id") && !body.containsKey("ServiceTypeID")) body.put("ServiceTypeID", body.get("service_type_id"));

        // local validation
        List<String> missing = new ArrayList<>();
        if (!body.containsKey("FromDistrictID") || body.get("FromDistrictID") == null) missing.add("FromDistrictID");
        if (!body.containsKey("ToDistrictID") || body.get("ToDistrictID") == null) missing.add("ToDistrictID");
        if (!body.containsKey("ShopID") || body.get("ShopID") == null) missing.add("ShopID");
        if (!body.containsKey("weight") || body.get("weight") == null) missing.add("weight");

        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Missing required GHN fields: " + String.join(", ", missing));
        }
    }

    /**
     * Helper to wrap body under "myRequest" (GHN sometimes expects this envelope).
     */
    private Map<String, Object> wrapMyRequest(Map<String, Object> body) {
        Map<String, Object> wrapped = new HashMap<>();
        wrapped.put("myRequest", body);
        return wrapped;
    }

    public String toJson(Object obj) {
         try { return objectMapper.writeValueAsString(obj); }
         catch (JsonProcessingException e) { return "{}"; }
     }

    /**
     * Calculate fee endpoint
     */
    public Map<String, Object> calculateFee(Map<String, Object> payload) {
        return calculateFee(payload, null);
    }

    public Map<String, Object> calculateFee(Map<String, Object> payload, Long shopId) {
        String url = ghnApiUrl.replaceAll("/$", "") + "/v2/shipping-order/fee";
        Map<String, Object> body = new HashMap<>(payload == null ? Map.of() : payload);

        if (!body.containsKey("from_district_id") && shopId != null) populateFromByShop(body, shopId);

        if (shopId != null) {
            try {
                Optional<Shop> os = shopRepository.findById(shopId);
                if (os.isPresent()) {
                    Shop s = os.get();
                    if (s.getGhnToken() == null || s.getGhnToken().isBlank()) {
                        Map<String,Object> err = new HashMap<>();
                        err.put("code", 400);
                        err.put("message", "Missing per-shop GHN token for shopId=" + shopId + ". Please set via PUT /api/shops/" + shopId + "/ghn-credentials");
                        err.put("_local_validation", true);
                        return err;
                    }
                }
            } catch (Exception ignored) {}
        }

        if (!body.containsKey("from_district_id") && ghnFromDistrictId != null && !ghnFromDistrictId.isBlank()) {
            try { body.put("from_district_id", Integer.parseInt(ghnFromDistrictId)); }
            catch (Exception ex) { body.put("from_district_id", ghnFromDistrictId); }
        }
        if (!body.containsKey("from_ward_code") && ghnFromWardCode != null && !ghnFromWardCode.isBlank()) {
            body.put("from_ward_code", ghnFromWardCode);
        }

        if (!body.containsKey("service_id") && ghnDefaultServiceId != null && !ghnDefaultServiceId.isBlank()) {
            try { body.put("service_id", Integer.parseInt(ghnDefaultServiceId)); }
            catch (Exception ex) { body.put("service_id", ghnDefaultServiceId); }
        }
        if (!body.containsKey("service_type_id") && ghnDefaultServiceTypeId != null && !ghnDefaultServiceTypeId.isBlank()) {
            try { body.put("service_type_id", Integer.parseInt(ghnDefaultServiceTypeId)); }
            catch (Exception ex) { body.put("service_type_id", ghnDefaultServiceTypeId); }
        }

        try {
            ensureGhnFieldVariants(body, shopId);
        } catch (IllegalArgumentException ex) {
            Map<String,Object> err = new HashMap<>();
            err.put("code", 400);
            err.put("message", ex.getMessage());
            err.put("_local_validation", true);
            return err;
        }

        Map<String, Object> wrapped = wrapMyRequest(body);
        logger.debug("GHN fee request body (wrapped): {}", toJson(wrapped));
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(wrapped, baseHeaders(shopId));
        logHeaders(req.getHeaders());

        try {
            ResponseEntity<Map> resp = restTemplate.postForEntity(url, req, Map.class);
            if (resp == null || resp.getBody() == null) throw new RuntimeException("Empty response from GHN fee API");
            @SuppressWarnings("unchecked")
            Map<String,Object> mp = resp.getBody();
            return mp;
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            String respBody = ex.getResponseBodyAsString();
            logger.warn("GHN fee returned error: {} {}", ex.getStatusCode(), respBody);
            try { Map<String,Object> parsed = objectMapper.readValue(respBody, Map.class); parsed.putIfAbsent("_http_status", ex.getStatusCode().value()); return parsed; }
            catch (Exception e) { Map<String,Object> fallback = new HashMap<>(); fallback.put("code", ex.getStatusCode().value()); fallback.put("message", respBody); return fallback; }
        }
    }

    /**
     * Available services endpoint
     */
    public Map<String, Object> getAvailableServices(Map<String, Object> payload) {
        return getAvailableServices(payload, null);
    }

    public Map<String, Object> getAvailableServices(Map<String, Object> payload, Long shopId) {
        String url = ghnApiUrl.replaceAll("/$", "") + "/v2/shipping-order/available-services";

        // defensive copy of incoming payload
        Map<String, Object> src = payload == null ? Map.of() : new HashMap<>(payload);

        // ensure from_* populated from shop if missing
        if (!src.containsKey("from_district_id") && shopId != null) populateFromByShop(src, shopId);

        // build canonical request map (PascalCase keys GHN expects)
        Map<String, Object> reqBody = new LinkedHashMap<>();
        try {
            // districts / shop / weight as integers (if present)
            Object fd = src.containsKey("FromDistrictID") ? src.get("FromDistrictID") : src.get("from_district_id");
            if (fd != null) reqBody.put("FromDistrictID", Integer.parseInt(String.valueOf(fd)));

            Object td = src.containsKey("ToDistrictID") ? src.get("ToDistrictID") : src.get("to_district_id");
            if (td != null) reqBody.put("ToDistrictID", Integer.parseInt(String.valueOf(td)));

            Object shopRaw = src.containsKey("ShopID") ? src.get("ShopID") : src.get("shop_id");
            if (shopRaw != null) {
                try { reqBody.put("ShopID", Integer.parseInt(String.valueOf(shopRaw))); }
                catch (NumberFormatException nfe) { reqBody.put("ShopID", String.valueOf(shopRaw)); }
            }

            Object w = src.containsKey("Weight") ? src.get("Weight") : src.get("weight");
            if (w != null) reqBody.put("Weight", Integer.parseInt(String.valueOf(w)));
        } catch (Exception ex) {
            // don't fail here; GHN will validate — but log for debugging
            logger.debug("normalize numeric fields failed: {}", ex.getMessage());
        }

        // wards and other fields as strings / pass-through
        if (src.containsKey("FromWardCode")) reqBody.put("FromWardCode", String.valueOf(src.get("FromWardCode")));
        else if (src.containsKey("from_ward_code")) reqBody.put("FromWardCode", String.valueOf(src.get("from_ward_code")));

        if (src.containsKey("ToWardCode")) reqBody.put("ToWardCode", String.valueOf(src.get("ToWardCode")));
        else if (src.containsKey("to_ward_code")) reqBody.put("ToWardCode", String.valueOf(src.get("to_ward_code")));

        if (src.containsKey("insurance_value")) reqBody.put("insurance_value", src.get("insurance_value"));

        // log canonical body (single, clean map)
        logger.info("DEBUG -> GHN available-services request (canonical): {}", toJson(reqBody));

        HttpHeaders headers = baseHeaders(shopId);
        headers.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));
        logHeaders(headers);

        try {
            ResponseEntity<String> resp = restTemplate.postForEntity(url, new HttpEntity<>(reqBody, headers), String.class);
            logger.info("DEBUG -> GHN response status={} body={}", resp.getStatusCodeValue(), resp.getBody());
            if (resp == null || resp.getBody() == null) throw new RuntimeException("Empty response from GHN available-services API");
            return objectMapper.readValue(resp.getBody(), Map.class);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            String respBody = ex.getResponseBodyAsString();
            logger.warn("GHN available-services returned error: {} {}", ex.getStatusCode(), respBody);
            try { Map<String,Object> parsed = objectMapper.readValue(respBody, Map.class); parsed.putIfAbsent("_http_status", ex.getStatusCode().value()); return parsed; }
            catch (Exception e) { Map<String,Object> fallback = new HashMap<>(); fallback.put("code", ex.getStatusCode().value()); fallback.put("message", respBody); return fallback; }
        } catch (Exception ex) {
            logger.warn("GHN available-services unexpected error: {}", ex.getMessage());
            Map<String,Object> fallback = new HashMap<>(); fallback.put("code", 500); fallback.put("message", ex.getMessage()); return fallback;
        }
    }

    /**
     * Create shipping order
     */
    public Map<String, Object> createShippingOrder(Map<String, Object> payload) {
        return createShippingOrder(payload, null);
    }

    public Map<String, Object> createShippingOrder(Map<String, Object> payload, Long shopId) {
        String url = ghnApiUrl.replaceAll("/$", "") + "/v2/shipping-order/create";
        Map<String, Object> body = new HashMap<>(payload == null ? Map.of() : payload);

        if (!body.containsKey("from_district_id") && shopId != null) populateFromByShop(body, shopId);

        if (shopId != null) {
            try {
                Optional<Shop> os = shopRepository.findById(shopId);
                if (os.isPresent()) {
                    Shop s = os.get();
                    if (s.getGhnToken() == null || s.getGhnToken().isBlank()) {
                        Map<String,Object> err = new HashMap<>();
                        err.put("code", 400);
                        err.put("message", "Missing per-shop GHN token for shopId=" + shopId + ". Please set via PUT /api/shops/" + shopId + "/ghn-credentials");
                        err.put("_local_validation", true);
                        return err;
                    }
                }
            } catch (Exception ignored) {}
        }

        if (!body.containsKey("shop_id") && shopId != null) body.put("shop_id", shopId);

        if (!body.containsKey("required_note")) body.put("required_note", "KHONGCHOXEMHANG");

        if (!body.containsKey("service_id") && ghnDefaultServiceId != null && !ghnDefaultServiceId.isBlank()) {
            try { body.put("service_id", Integer.parseInt(ghnDefaultServiceId)); }
            catch (Exception ex) { body.put("service_id", ghnDefaultServiceId); }
        }

        if (!body.containsKey("payment_type_id")) {
            Object codObj = body.get("cod_amount");
            int cod = 0;
            if (codObj instanceof Number) cod = ((Number) codObj).intValue();
            else if (codObj instanceof String) {
                try { cod = Integer.parseInt((String) codObj); } catch (Exception ignored) {}
            }
            body.put("payment_type_id", cod > 0 ? 2 : 1);
        }

        try {
            ensureGhnFieldVariants(body, shopId);
        } catch (IllegalArgumentException ex) {
            Map<String,Object> err = new HashMap<>();
            err.put("code", 400);
            err.put("message", ex.getMessage());
            err.put("_local_validation", true);
            return err;
        }

        Map<String, Object> wrapped = wrapMyRequest(body);
        logger.debug("GHN create-order request body (wrapped): {}", toJson(wrapped));
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(wrapped, baseHeaders(shopId));
        logHeaders(req.getHeaders());

        try {
            ResponseEntity<Map> resp = restTemplate.postForEntity(url, req, Map.class);
            if (resp == null || resp.getBody() == null) throw new RuntimeException("Empty response from GHN create API");
            @SuppressWarnings("unchecked")
            Map<String,Object> mp = resp.getBody();
            return mp;
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            String respBody = ex.getResponseBodyAsString();
            logger.warn("GHN create returned error: {} {}", ex.getStatusCode(), respBody);
            try { Map<String,Object> parsed = objectMapper.readValue(respBody, Map.class); parsed.putIfAbsent("_http_status", ex.getStatusCode().value()); return parsed; }
            catch (Exception e) { Map<String,Object> fallback = new HashMap<>(); fallback.put("code", ex.getStatusCode().value()); fallback.put("message", respBody); return fallback; }
        }
    }

    /**
     * Create/register GHN shop (returns external shop id string or null)
     */
    public String createGhnShop(Map<String, Object> payload) {
        String url = ghnApiUrl.replaceAll("/$", "") + "/v2/shop/register";
        Map<String, Object> body = new HashMap<>(payload == null ? Map.of() : payload);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, baseHeaders(null));
        try {
            ResponseEntity<Map> resp = restTemplate.postForEntity(url, req, Map.class);
            if (resp == null || resp.getBody() == null) return null;
            Object data = resp.getBody().get("data");
            if (data instanceof Map) {
                Map<?,?> m = (Map<?,?>) data;
                Object sid = m.get("shop_id");
                if (sid == null) sid = m.get("id");
                if (sid == null) sid = m.get("ShopID");
                if (sid != null) return String.valueOf(sid);
            }
            Object sid = resp.getBody().get("shop_id");
            if (sid == null) sid = resp.getBody().get("id");
            if (sid != null) return String.valueOf(sid);
            logger.warn("GHN create shop responded but no shop id found: {}", resp.getBody());
            return null;
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            logger.warn("GHN create shop returned error: {} {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            return null;
        } catch (Exception ex) {
            logger.warn("Unexpected error creating GHN shop: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Get order detail
     */
    public Map<String, Object> getOrderDetail(String orderCode) {
        String url = ghnApiUrl.replaceAll("/$", "") + "/v2/shipping-order/detail?order_code=" + orderCode;
        HttpEntity<Void> req = new HttpEntity<>(baseHeaders());
        logHeaders(req.getHeaders());
        ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.GET, req, Map.class);
        if (resp == null || resp.getBody() == null) throw new RuntimeException("Empty response from GHN detail API");
        @SuppressWarnings("unchecked")
        Map<String,Object> mp = resp.getBody();
        return mp;
    }

    /**
     * Create DB Shipment record best-effort
     */
    public Shipment createShippingOrderAsync(Long orderId, Map<String, Object> payload) {
        Map<String, Object> resp;
        try {
            resp = createShippingOrder(payload);
        } catch (Exception ex) {
            Shipment fail = new Shipment();
            fail.setOrderId(orderId);
            fail.setReceiverName(String.valueOf(payload.getOrDefault("to_name", null)));
            fail.setReceiverPhone(String.valueOf(payload.getOrDefault("to_phone", null)));
            fail.setReceiverAddress(String.valueOf(payload.getOrDefault("to_address", null)));
            fail.setProvince(String.valueOf(payload.getOrDefault("province", null)));
            fail.setDistrict(String.valueOf(payload.getOrDefault("district", null)));
            fail.setWard(String.valueOf(payload.getOrDefault("ward", null)));
            fail.setStatus("GHN_ERROR");
            fail.setGhnPayload(toJson(Map.of("error", ex.getMessage(), "request", payload)));
            return shipmentRepository.save(fail);
        }

        Shipment s = new Shipment();
        s.setOrderId(orderId);
        s.setReceiverName(String.valueOf(payload.getOrDefault("to_name", null)));
        s.setReceiverPhone(String.valueOf(payload.getOrDefault("to_phone", null)));
        s.setReceiverAddress(String.valueOf(payload.getOrDefault("to_address", null)));
        s.setProvince(String.valueOf(payload.getOrDefault("province", null)));
        s.setDistrict(String.valueOf(payload.getOrDefault("district", null)));
        s.setWard(String.valueOf(payload.getOrDefault("ward", null)));

        Object pFee = payload.get("shipping_fee");
        if (pFee instanceof BigDecimal) s.setShippingFee((BigDecimal) pFee);
        else if (pFee instanceof Number) s.setShippingFee(BigDecimal.valueOf(((Number) pFee).doubleValue()));

        Object data = resp.get("data");
        if (data instanceof Map) {
            Map<?, ?> dataMap = (Map<?, ?>) data;
            if (s.getShippingFee() == null && dataMap.get("total_fee") instanceof Number) {
                s.setShippingFee(BigDecimal.valueOf(((Number) dataMap.get("total_fee")).doubleValue()));
            }
            Object oc = dataMap.get("order_code");
            if (oc != null) s.setGhnOrderCode(String.valueOf(oc));
            Object expectedTime = dataMap.get("expected_delivery_time");
            if (expectedTime instanceof String) {
                try { s.setExpectedDelivery(LocalDateTime.parse((String) expectedTime)); }
                catch (DateTimeParseException ignored) {}
            }
            Object transType = dataMap.get("trans_type");
            if (transType != null) s.setServiceType(String.valueOf(transType));
        }

        s.setStatus("CREATED");
        s.setGhnPayload(toJson(resp));
        return shipmentRepository.save(s);
    }
}