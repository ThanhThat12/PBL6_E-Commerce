package com.PBL6.Ecommerce.controller.shipping;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.service.GhnMasterDataService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "GHN Master Data", description = "GHN provinces, districts, wards data for address selection. These endpoints are used by frontend to populate location dropdowns.")
@RestController
@RequestMapping("/api/ghn/master")
public class GhnMasterController {

    private final GhnMasterDataService ghn;

    public GhnMasterController(GhnMasterDataService ghn) {
        this.ghn = ghn;
    }

    @Operation(
        summary = "Get all provinces/cities",
        description = "Fetch list of all provinces and cities in Vietnam from GHN master data. Used for province/city selection dropdown."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved provinces list",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class),
                examples = @ExampleObject(
                    name = "Provinces Response",
                    value = "[{\"ProvinceID\":202,\"ProvinceName\":\"H\u1ed3 Ch\u00ed Minh\"},{\"ProvinceID\":201,\"ProvinceName\":\"H\u00e0 N\u1ed9i\"}]"
                )
            )
        )
    })
    @GetMapping("/provinces")
    public ResponseEntity<List<Map<String,Object>>> provinces() {
        return ResponseEntity.ok(ghn.getProvinces());
    }

    @Operation(
        summary = "Get districts by province",
        description = "Fetch list of districts/counties for a specific province from GHN. Used for district selection dropdown after province is selected."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved districts list",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class),
                examples = @ExampleObject(
                    name = "Districts Response",
                    value = "[{\"DistrictID\":1450,\"DistrictName\":\"Qu\u1eadn 1\"},{\"DistrictID\":1451,\"DistrictName\":\"Qu\u1eadn 3\"}]"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid province_id")
    })
    @GetMapping("/districts")
    public ResponseEntity<List<Map<String,Object>>> districts(
        @Parameter(
            description = "GHN Province ID (ProvinceID from provinces endpoint)",
            required = true,
            example = "202"
        )
        @RequestParam Integer province_id
    ) {
        return ResponseEntity.ok(ghn.getDistricts(province_id));
    }

    @Operation(
        summary = "Get wards by district",
        description = "Fetch list of wards/communes for a specific district from GHN. Used for ward selection dropdown after district is selected."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved wards list",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class),
                examples = @ExampleObject(
                    name = "Wards Response",
                    value = "[{\"WardCode\":\"21012\",\"WardName\":\"Ph\u01b0\u1eddng B\u1ebfn Ngh\u00e9\"},{\"WardCode\":\"21013\",\"WardName\":\"Ph\u01b0\u1eddng B\u1ebfn Th\u00e0nh\"}]"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid district_id")
    })
    @GetMapping("/wards")
    public ResponseEntity<List<Map<String,Object>>> wards(
        @Parameter(
            description = "GHN District ID (DistrictID from districts endpoint)",
            required = true,
            example = "1450"
        )
        @RequestParam Integer district_id
    ) {
        return ResponseEntity.ok(ghn.getWards(district_id));
    }

    @Operation(
        summary = "Resolve address names to IDs",
        description = "Convert human-readable province/district/ward names to GHN IDs/codes. Used when user enters names instead of selecting from dropdowns. Supports fuzzy matching."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully resolved address",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Resolve Response",
                    value = "{\"province\":{\"id\":202,\"name\":\"H\u1ed3 Ch\u00ed Minh\"},\"district\":{\"id\":1450,\"name\":\"Qu\u1eadn 1\"},\"ward\":{\"code\":\"21012\",\"name\":\"Ph\u01b0\u1eddng B\u1ebfn Ngh\u00e9\"}}"
                )
            )
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Address names to resolve",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Resolve Request",
                value = "{\"province\":\"H\u1ed3 Ch\u00ed Minh\",\"district\":\"Qu\u1eadn 1\",\"ward\":\"B\u1ebfn Ngh\u00e9\"}"
            )
        )
    )
    @PostMapping("/resolve")
    public ResponseEntity<Map<String,Object>> resolve(@RequestBody Map<String,String> body) {
        String province = body.get("province");
        String district = body.get("district");
        String ward = body.get("ward");
        Map<String,Object> result = ghn.resolveAddress(province, district, ward);
        return ResponseEntity.ok(result);
    }
}