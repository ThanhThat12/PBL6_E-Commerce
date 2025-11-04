package com.PBL6.Ecommerce.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GhnMasterDataService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ghn.api.url}")
    private String ghnApiUrl;

    @Value("${ghn.token}")
    private String ghnToken;

    public GhnMasterDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private HttpHeaders baseHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", ghnToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String,Object>> getProvinces() {
        String url = ghnApiUrl + "/master-data/province";
        ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(baseHeaders()), Map.class);
        if (!resp.getStatusCode().is2xxSuccessful()) return Collections.emptyList();
        Object d = resp.getBody().get("data");
        return d instanceof List ? (List<Map<String,Object>>) d : Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String,Object>> getDistricts(Integer provinceId) {
        if (provinceId == null) return Collections.emptyList();
        String url = ghnApiUrl + "/master-data/district?province_id=" + provinceId;
        ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(baseHeaders()), Map.class);
        if (!resp.getStatusCode().is2xxSuccessful()) return Collections.emptyList();
        Object d = resp.getBody().get("data");
        return d instanceof List ? (List<Map<String,Object>>) d : Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String,Object>> getWards(Integer districtId) {
        if (districtId == null) return Collections.emptyList();
        String url = ghnApiUrl + "/master-data/ward?district_id=" + districtId;
        ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(baseHeaders()), Map.class);
        if (!resp.getStatusCode().is2xxSuccessful()) return Collections.emptyList();
        Object d = resp.getBody().get("data");
        return d instanceof List ? (List<Map<String,Object>>) d : Collections.emptyList();
    }

    // Resolve by fuzzy name matching: client can send province/district/ward names (partial)
    public Map<String,Object> resolveAddress(String provinceName, String districtName, String wardName) {
        Map<String,Object> out = new HashMap<>();
        List<Map<String,Object>> provinces = getProvinces();
        Map<String,Object> foundProvince = findByName(provinces, provinceName, "ProvinceName");
        if (foundProvince != null) {
            Integer provinceId = toInt(foundProvince.get("ProvinceID"));
            out.put("province", Map.of(
                    "id", provinceId,
                    "name", foundProvince.get("ProvinceName")
            ));
            List<Map<String,Object>> districts = getDistricts(provinceId);
            Map<String,Object> foundDistrict = findByName(districts, districtName, "DistrictName");
            if (foundDistrict != null) {
                Integer districtId = toInt(foundDistrict.get("DistrictID"));
                out.put("district", Map.of(
                        "id", districtId,
                        "name", foundDistrict.get("DistrictName")
                ));
                List<Map<String,Object>> wards = getWards(districtId);
                Map<String,Object> foundWard = findByName(wards, wardName, "WardName");
                if (foundWard != null) {
                    out.put("ward", Map.of(
                            "code", String.valueOf(foundWard.get("WardCode")),
                            "name", foundWard.get("WardName")
                    ));
                } else {
                    // try to match by WardCode if client gave code
                    if (wardName != null && !wardName.isBlank()) {
                        List<Map<String,Object>> matches = wards.stream()
                                .filter(w -> wardName.equalsIgnoreCase(String.valueOf(w.get("WardCode"))))
                                .collect(Collectors.toList());
                        if (!matches.isEmpty()) {
                            var w = matches.get(0);
                            out.put("ward", Map.of("code", String.valueOf(w.get("WardCode")), "name", w.get("WardName")));
                        }
                    }
                }
            } else {
                // not found district -> optionally search across all districts for best match
                // omitted for brevity
            }
        } else {
            // not found province -> optionally fuzzy search across provinces
        }
        return out;
    }

    private Map<String,Object> findByName(List<Map<String,Object>> list, String name, String key) {
        if (name == null || name.isBlank()) return null;
        String q = name.trim().toLowerCase();
        // exact contains match first
        for (Map<String,Object> m : list) {
            Object v = m.get(key);
            if (v != null && v.toString().toLowerCase().contains(q)) return m;
        }
        // fallback: startsWith
        for (Map<String,Object> m : list) {
            Object v = m.get(key);
            if (v != null && v.toString().toLowerCase().startsWith(q)) return m;
        }
        return null;
    }

    private Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).intValue();
        try { return Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return null; }
    }
}