package com.PBL6.Ecommerce.controller.shipping;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.dto.AddressRequestDTO;
import com.PBL6.Ecommerce.domain.dto.AddressResponseDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.entity.user.Address;
import com.PBL6.Ecommerce.service.AddressService;
import com.PBL6.Ecommerce.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
@Tag(name = "Addresses", description = "User address management")
@RestController
@RequestMapping("/api/me/addresses")
public class AddressController {

    private final AddressService addressService;
    private final UserService userService;

    public AddressController(AddressService addressService, UserService userService) {
        this.addressService = addressService;
        this.userService = userService;
    }

    private AddressResponseDTO toDto(Address a) {
        if (a == null) return null;
        AddressResponseDTO d = new AddressResponseDTO();
        d.setId(a.getId());
        d.setFullAddress(a.getFullAddress());
        d.setProvinceId(a.getProvinceId());
        d.setDistrictId(a.getDistrictId());
        d.setWardCode(a.getWardCode());
        
        // Use stored names from entity
        d.setProvinceName(a.getProvinceName());
        d.setDistrictName(a.getDistrictName());
        d.setWardName(a.getWardName());
        
        d.setContactName(a.getContactName());
        d.setContactPhone(a.getContactPhone());
        d.setPrimaryAddress(a.isPrimaryAddress());
        d.setTypeAddress(a.getTypeAddress() != null ? a.getTypeAddress().name() : "HOME");
        d.setCreatedAt(a.getCreatedAt());
        d.setUpdatedAt(a.getUpdatedAt());
        return d;
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<List<AddressResponseDTO>>> list(@AuthenticationPrincipal Jwt jwt) {
        Long userId = userService.extractUserIdFromJwt(jwt);
        List<Address> list = addressService.listByUser(userId);
        List<AddressResponseDTO> dtoList = list.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseDTO.success(dtoList, "Lấy danh sách địa chỉ thành công");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<AddressResponseDTO>> get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        Long userId = userService.extractUserIdFromJwt(jwt);
        Address address = addressService.getByIdAndUser(id, userId);
        return ResponseDTO.success(toDto(address), "Lấy thông tin địa chỉ thành công");
    }

    @PostMapping
    @Operation(
        summary = "Create new address",
        description = "Create a new address for the current user.\\n\\n" +
                      "**Auto Primary Logic:**\\n" +
                      "- When primaryAddress=true for HOME type, automatically unsets primary flag for ALL other HOME addresses\\n" +
                      "- Ensures only one primary address per user\\n\\n" +
                      "**Type Constraints:**\\n" +
                      "- HOME: Unlimited, can have multiple, choose 1 as primary for delivery\\n" +
                      "- STORE: Maximum 1 per seller, cannot be set as primary\\n\\n" +
                      "**Response includes:** id, fullAddress, location names (province/district/ward), contactName, contactPhone, typeAddress, primaryAddress, createdAt, updatedAt",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "✅ Address created successfully. If primaryAddress=true, other HOME addresses automatically unmarked as primary."
        ),
        @ApiResponse(
            responseCode = "400",
            description = "❌ Bad Request - Validation errors:\\n" +
                         "• **typeAddress invalid** - Must be HOME or STORE only\\n" +
                         "• **STORE limit exceeded** - Seller already has 1 STORE address (can only have 1)\\n" +
                         "• **STORE cannot be primary** - Only HOME addresses can be marked as primary\\n" +
                         "• **Invalid phone** - Format must be Vietnamese (0 or +84 followed by 9-10 digits)\\n" +
                         "• **Invalid GHN IDs** - Province/district/ward codes not found in system"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "🔐 Unauthorized - JWT token missing, expired, or invalid"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "⚠️ User not found - The authenticated user does not exist in system"
        )
    })
    public ResponseEntity<ResponseDTO<AddressResponseDTO>> create(@AuthenticationPrincipal Jwt jwt, 
                                                                   @Valid @RequestBody AddressRequestDTO req) {
        Long userId = userService.extractUserIdFromJwt(jwt);
        Address address = addressService.createForUser(userId, req);
        return ResponseDTO.created(toDto(address), "Tạo địa chỉ thành công");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<AddressResponseDTO>> update(@AuthenticationPrincipal Jwt jwt, 
                                                                   @PathVariable Long id, 
                                                                   @Valid @RequestBody AddressRequestDTO req) {
        Long userId = userService.extractUserIdFromJwt(jwt);
        Address address = addressService.updateForUser(userId, id, req);
        return ResponseDTO.success(toDto(address), "Cập nhật địa chỉ thành công");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<Void>> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        Long userId = userService.extractUserIdFromJwt(jwt);
        addressService.deleteForUser(userId, id);
        return ResponseDTO.success(null, "Xóa địa chỉ thành công");
    }

    @PostMapping("/{id}/primary")
    public ResponseEntity<ResponseDTO<AddressResponseDTO>> markPrimary(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        Long userId = userService.extractUserIdFromJwt(jwt);
        Address address = addressService.markPrimary(userId, id);
        return ResponseDTO.success(toDto(address), "Đánh dấu địa chỉ chính thành công");
    }
}