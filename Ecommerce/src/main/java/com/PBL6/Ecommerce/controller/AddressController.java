package com.PBL6.Ecommerce.controller;

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

import com.PBL6.Ecommerce.domain.Address;
import com.PBL6.Ecommerce.domain.dto.AddressRequestDTO;
import com.PBL6.Ecommerce.domain.dto.AddressResponseDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.service.AddressService;
import com.PBL6.Ecommerce.service.UserService;

import jakarta.validation.Valid;

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
        d.setLabel(a.getLabel());
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
        d.setCreatedAt(a.getCreatedAt());
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