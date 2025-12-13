package com.PBL6.Ecommerce.controller;

import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.service.GhnMasterDataService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Locations Controller per spec 006-profile Phase 4 (US2) Task T022
 * Proxy endpoints for GHN master data with caching
 * Provides province/district/ward cascading data for address forms
 */
@Tag(name = "Locations", description = "GHN location master data (provinces, districts, wards)")
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Slf4j
public class LocationsController {

    private final GhnMasterDataService ghnMaster;

    @Operation(summary = "Get all provinces", description = "Retrieve list of all Vietnamese provinces from GHN (cached 24h)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Provinces retrieved successfully")
    })
    @GetMapping("/provinces")
    @Cacheable(value = "provinces", unless = "#result == null || #result.isEmpty()")
    public ResponseEntity<ResponseDTO<List<Map<String, Object>>>> getProvinces() {
        log.debug("Fetching provinces from GHN");
        List<Map<String, Object>> provinces = ghnMaster.getProvinces();
        return ResponseDTO.success(provinces, "Provinces retrieved successfully");
    }

    @Operation(summary = "Get districts by province", description = "Retrieve list of districts for a province from GHN (cached 24h)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Districts retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid province ID")
    })
    @GetMapping("/districts/{provinceId}")
    @Cacheable(value = "districts", key = "#provinceId", unless = "#result == null || #result.isEmpty()")
    public ResponseEntity<ResponseDTO<List<Map<String, Object>>>> getDistricts(@PathVariable Integer provinceId) {
        log.debug("Fetching districts for province: {}", provinceId);
        List<Map<String, Object>> districts = ghnMaster.getDistricts(provinceId);
        return ResponseDTO.success(districts, "Districts retrieved successfully");
    }

    @Operation(summary = "Get wards by district", description = "Retrieve list of wards for a district from GHN (cached 24h)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Wards retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid district ID")
    })
    @GetMapping("/wards/{districtId}")
    @Cacheable(value = "wards", key = "#districtId", unless = "#result == null || #result.isEmpty()")
    public ResponseEntity<ResponseDTO<List<Map<String, Object>>>> getWards(@PathVariable Integer districtId) {
        log.debug("Fetching wards for district: {}", districtId);
        List<Map<String, Object>> wards = ghnMaster.getWards(districtId);
        return ResponseDTO.success(wards, "Wards retrieved successfully");
    }
}
