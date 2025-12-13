package com.PBL6.Ecommerce.controller.shipping;

import com.PBL6.Ecommerce.service.GhnMasterDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


import io.swagger.v3.oas.annotations.tags.Tag;
@Tag(name = "GHN Master Data", description = "GHN provinces, districts, wards data")
@RestController
@RequestMapping("/api/ghn/master")
public class GhnMasterController {

    private final GhnMasterDataService ghn;

    public GhnMasterController(GhnMasterDataService ghn) {
        this.ghn = ghn;
    }

    @GetMapping("/provinces")
    public ResponseEntity<List<Map<String,Object>>> provinces() {
        return ResponseEntity.ok(ghn.getProvinces());
    }

    @GetMapping("/districts")
    public ResponseEntity<List<Map<String,Object>>> districts(@RequestParam Integer province_id) {
        return ResponseEntity.ok(ghn.getDistricts(province_id));
    }

    @GetMapping("/wards")
    public ResponseEntity<List<Map<String,Object>>> wards(@RequestParam Integer district_id) {
        return ResponseEntity.ok(ghn.getWards(district_id));
    }

    // resolve endpoint: client gửi partial names, server trả các mã phù hợp để lưu vào Address
    @PostMapping("/resolve")
    public ResponseEntity<Map<String,Object>> resolve(@RequestBody Map<String,String> body) {
        String province = body.get("province");
        String district = body.get("district");
        String ward = body.get("ward");
        Map<String,Object> result = ghn.resolveAddress(province, district, ward);
        return ResponseEntity.ok(result);
    }
}