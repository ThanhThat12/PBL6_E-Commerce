package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.domain.Address;
import com.PBL6.Ecommerce.domain.dto.AddressRequestDTO;
import com.PBL6.Ecommerce.domain.dto.AddressResponseDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.service.AddressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/me/addresses")
public class AddressController {

    private final AddressService svc;

    public AddressController(AddressService svc) {
        this.svc = svc;
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
        d.setContactPhone(a.getContactPhone());
        d.setPrimaryAddress(a.isPrimaryAddress());
        d.setCreatedAt(a.getCreatedAt());
        return d;
    }

    private Long extractUserId(Jwt jwt) {
        if (jwt == null) return null;
        String sub = jwt.getSubject();
        if (sub == null) return null;
        try { return Long.parseLong(sub); } catch (NumberFormatException ex) { return null; }
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<List<AddressResponseDTO>>> list(@AuthenticationPrincipal Jwt jwt) {
        Long userId = extractUserId(jwt);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDTO<>(401, "UNAUTHORIZED", "Invalid token subject", null));

        List<Address> list = svc.listByUser(userId);
        List<AddressResponseDTO> dtoList = list.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "OK", dtoList));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<AddressResponseDTO>> get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        Long userId = extractUserId(jwt);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDTO<>(401, "UNAUTHORIZED", "Invalid token subject", null));

        return svc.getByIdAndUser(id, userId)
                .map(a -> ResponseEntity.ok(new ResponseDTO<>(200, null, "OK", toDto(a))))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO<>(404, "NOT_FOUND", "Address not found", null)));
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<AddressResponseDTO>> create(@AuthenticationPrincipal Jwt jwt, @RequestBody AddressRequestDTO req) {
        Long userId = extractUserId(jwt);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDTO<>(401, "UNAUTHORIZED", "Invalid token subject", null));

        return svc.createForUser(userId, req)
                .map(a -> {
                    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                            .path("/{id}")
                            .buildAndExpand(a.getId())
                            .toUri();
                    ResponseDTO<AddressResponseDTO> dto = new ResponseDTO<>(201, null, "Created", toDto(a));
                    return ResponseEntity.created(location).body(dto);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO<>(404, "NOT_FOUND", "User not found", null)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<AddressResponseDTO>> update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @RequestBody AddressRequestDTO req) {
        Long userId = extractUserId(jwt);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDTO<>(401, "UNAUTHORIZED", "Invalid token subject", null));

        return svc.updateForUser(userId, id, req)
                .map(a -> ResponseEntity.ok(new ResponseDTO<>(200, null, "Updated", toDto(a))))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO<>(404, "NOT_FOUND", "Address not found", null)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<Void>> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        Long userId = extractUserId(jwt);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDTO<>(401, "UNAUTHORIZED", "Invalid token subject", null));

        boolean ok = svc.deleteForUser(userId, id);
        if (ok) {
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "Deleted", null));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(404, "NOT_FOUND", "Address not found", null));
        }
    }

    @PostMapping("/{id}/primary")
    public ResponseEntity<ResponseDTO<AddressResponseDTO>> markPrimary(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        Long userId = extractUserId(jwt);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDTO<>(401, "UNAUTHORIZED", "Invalid token subject", null));

        return svc.markPrimary(userId, id)
                .map(a -> ResponseEntity.ok(new ResponseDTO<>(200, null, "Marked primary", toDto(a))))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO<>(404, "NOT_FOUND", "Address not found", null)));
    }
}