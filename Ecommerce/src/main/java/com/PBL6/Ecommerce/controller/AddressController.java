package com.PBL6.Ecommerce.controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.Address;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.AddressRequestDTO;
import com.PBL6.Ecommerce.domain.dto.AddressResponseDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.dto.profile.AddressAutoFillResponse;
import com.PBL6.Ecommerce.dto.profile.CreateAddressRequest;
import com.PBL6.Ecommerce.dto.profile.UpdateAddressRequest;
import com.PBL6.Ecommerce.service.AddressService;
import com.PBL6.Ecommerce.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Address Controller per spec 006-profile Phase 4 (US2)
 * Manages user addresses with GHN location integration
 * Path aligned with spec: /api/addresses
 */
@Tag(name = "Addresses", description = "User address management with GHN locations")
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final UserService userService;

    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userService.resolveCurrentUser(authentication);
    }

    /**
     * Convert Address entity to response DTO
     */
    private AddressResponseDTO toDto(Address a) {
        if (a == null) return null;
        AddressResponseDTO d = new AddressResponseDTO();
        d.setId(a.getId());
        d.setFullAddress(a.getFullAddress());
        d.setProvinceId(a.getProvinceId());
        d.setDistrictId(a.getDistrictId());
        d.setWardCode(a.getWardCode());
        d.setProvinceName(a.getProvinceName());
        d.setDistrictName(a.getDistrictName());
        d.setWardName(a.getWardName());
        d.setContactName(a.getContactName());
        d.setContactPhone(a.getContactPhone());
        d.setTypeAddress(a.getTypeAddress());
        d.setPrimaryAddress(a.isPrimaryAddress());
        d.setCreatedAt(a.getCreatedAt());
        return d;
    }

    /**
     * Convert spec CreateAddressRequest to existing AddressRequestDTO
     */
    private AddressRequestDTO toRequestDTO(CreateAddressRequest req) {
        AddressRequestDTO dto = new AddressRequestDTO();
        dto.fullAddress = req.getFullAddress();
        dto.provinceId = req.getProvinceId();
        dto.districtId = req.getDistrictId();
        dto.wardCode = req.getWardCode();
        dto.provinceName = req.getProvinceName();
        dto.districtName = req.getDistrictName();
        dto.wardName = req.getWardName();
        dto.contactName = req.getContactName();
        dto.contactPhone = req.getContactPhone();
        dto.typeAddress = req.getTypeAddress();
        dto.primaryAddress = req.getPrimaryAddress() != null ? req.getPrimaryAddress() : false;
        return dto;
    }

    /**
     * Convert spec UpdateAddressRequest to existing AddressRequestDTO
     */
    private AddressRequestDTO toRequestDTO(UpdateAddressRequest req) {
        AddressRequestDTO dto = new AddressRequestDTO();
        dto.fullAddress = req.getFullAddress();
        dto.provinceId = req.getProvinceId();
        dto.districtId = req.getDistrictId();
        dto.wardCode = req.getWardCode();
        dto.provinceName = req.getProvinceName();
        dto.districtName = req.getDistrictName();
        dto.wardName = req.getWardName();
        dto.contactName = req.getContactName();
        dto.contactPhone = req.getContactPhone();
        dto.typeAddress = req.getTypeAddress();
        dto.primaryAddress = req.getPrimaryAddress() != null ? req.getPrimaryAddress() : false;
        return dto;
    }

    @Operation(summary = "List all addresses", description = "Get all addresses for current user, sorted with primary first. Supports filtering by type.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Addresses retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    @GetMapping
    public ResponseEntity<ResponseDTO<List<AddressResponseDTO>>> list(
            @RequestParam(required = false) List<String> excludeTypes,
            @RequestParam(required = false) List<String> types) {
        User user = getCurrentUser();
        List<Address> list = addressService.listByUser(user.getId());
        
        // Filter by excludeTypes (e.g., excludeTypes=STORE for buyer view)
        if (excludeTypes != null && !excludeTypes.isEmpty()) {
            list = list.stream()
                .filter(a -> a.getTypeAddress() == null || !excludeTypes.contains(a.getTypeAddress().name()))
                .collect(Collectors.toList());
        }
        
        // Filter by types (e.g., types=HOME,SHIPPING)
        if (types != null && !types.isEmpty()) {
            list = list.stream()
                .filter(a -> a.getTypeAddress() != null && types.contains(a.getTypeAddress().name()))
                .collect(Collectors.toList());
        }
        
        // Sort: primary first, then by creation date desc
        List<AddressResponseDTO> dtoList = list.stream()
            .sorted(Comparator.comparing(Address::isPrimaryAddress).reversed()
                .thenComparing(Comparator.comparing(Address::getCreatedAt).reversed()))
            .map(this::toDto)
            .collect(Collectors.toList());
        return ResponseDTO.success(dtoList, "Addresses retrieved successfully");
    }

    @Operation(summary = "Get address by ID", description = "Get single address details with ownership check")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Address retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - not owner"),
        @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<AddressResponseDTO>> get(@PathVariable Long id) {
        User user = getCurrentUser();
        Address address = addressService.getByIdAndUser(id, user.getId());
        return ResponseDTO.success(toDto(address), "Address retrieved successfully");
    }

    @Operation(summary = "Create new address", description = "Create address; first address auto-set as primary; enforce single STORE per user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Address created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error or duplicate STORE address"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<ResponseDTO<AddressResponseDTO>> create(@Valid @RequestBody CreateAddressRequest req) {
        User user = getCurrentUser();
        Address address = addressService.createForUser(user.getId(), toRequestDTO(req));
        return ResponseDTO.created(toDto(address), "Address created successfully");
    }

    @Operation(summary = "Update address", description = "Update address fields; preserve primary unless explicitly changed")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Address updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - not owner"),
        @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<AddressResponseDTO>> update(@PathVariable Long id, 
                                                                   @Valid @RequestBody UpdateAddressRequest req) {
        User user = getCurrentUser();
        Address address = addressService.updateForUser(user.getId(), id, toRequestDTO(req));
        return ResponseDTO.success(toDto(address), "Address updated successfully");
    }

    @Operation(summary = "Delete address", description = "Delete address by ID with ownership check")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Address deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - not owner"),
        @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User user = getCurrentUser();
        addressService.deleteForUser(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Set primary address", description = "Atomically set this address as primary and unset others")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Primary address set successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - not owner"),
        @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @PutMapping("/{id}/set-primary")
    public ResponseEntity<ResponseDTO<AddressResponseDTO>> setPrimary(@PathVariable Long id) {
        User user = getCurrentUser();
        Address address = addressService.markPrimary(user.getId(), id);
        return ResponseDTO.success(toDto(address), "Primary address set successfully");
    }

    @Operation(summary = "Auto-fill contact info", description = "Get contact name and phone from user profile for address creation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contact info retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/auto-fill")
    public ResponseEntity<ResponseDTO<AddressAutoFillResponse>> autoFill() {
        User user = getCurrentUser();
        AddressAutoFillResponse response = new AddressAutoFillResponse(
            user.getFullName(),
            user.getPhoneNumber()
        );
        return ResponseDTO.success(response, "Contact info retrieved successfully");
    }
}