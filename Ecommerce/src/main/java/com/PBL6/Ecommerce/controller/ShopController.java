package com.PBL6.Ecommerce.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.jwt.Jwt;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.ShopAnalyticsDTO;
import com.PBL6.Ecommerce.domain.dto.ShopDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateShopDTO;
import com.PBL6.Ecommerce.service.ShopService;
import com.PBL6.Ecommerce.service.UserService;
import com.PBL6.Ecommerce.service.GhnService;
import java.util.HashMap;
import java.util.Map;
import com.PBL6.Ecommerce.domain.dto.GhnCredentialsDTO;
import com.PBL6.Ecommerce.domain.dto.GhnServiceSelectionDTO;
import com.PBL6.Ecommerce.domain.dto.ShopRegistrationDTO;

import jakarta.validation.Valid;
import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api")
public class ShopController {
    
    private final ShopService shopService;
    private final UserService userService;
    private final GhnService ghnService;
    
    public ShopController(ShopService shopService, UserService userService, GhnService ghnService) {
        this.shopService = shopService;
        this.userService = userService;
        this.ghnService = ghnService;
    }

    // /**
    //  * Lấy danh sách dịch vụ GHN khả dụng cho shop (frontend gọi để hiển thị lựa chọn service)
    //  * GET /api/shops/{shopId}/ghn-services?toDistrictId=...&toWardCode=...&weight=...
    //  * This endpoint is public (no owner requirement) so checkout flows can call it.
    //  */
    // @GetMapping("/shops/{shopId}/ghn-services")
    // public ResponseEntity<ResponseDTO<Map<String, Object>>> getGhnServices(
    //         @PathVariable Long shopId,
    //         @RequestParam(required = false) Integer toDistrictId,
    //         @RequestParam(required = false) String toWardCode,
    //         @RequestParam(required = false) Integer weight) {
    //     try {
    //         // build payload for GHN
    //         Map<String, Object> payload = new HashMap<>();
    //         if (toDistrictId != null) payload.put("to_district_id", toDistrictId);
    //         if (toWardCode != null) payload.put("to_ward_code", toWardCode);
    //         if (weight != null) payload.put("weight", weight);

    //         // call GhnService, letting it populate from_* via shop pickupAddress
    //         Map<String, Object> resp = ghnService.getAvailableServices(payload, shopId);
    //         return ResponseEntity.ok(new ResponseDTO<>(200, null, "Lấy danh sách dịch vụ thành công", resp));
    //     } catch (Exception e) {
    //         return ResponseEntity.status(400).body(new ResponseDTO<>(400, e.getMessage(), "Lấy danh sách dịch vụ thất bại", null));
    //     }
    // }

    // /**
    //  * Lưu selection service_id/service_type_id cho shop
    //  * PUT /api/shops/{shopId}/ghn-service-selection
    //  */
    // @PutMapping("/shops/{shopId}/ghn-service-selection")
    // @PreAuthorize("hasRole('SELLER')")
    // public ResponseEntity<ResponseDTO<com.PBL6.Ecommerce.domain.dto.ShopDTO>> selectGhnService(
    //         @PathVariable Long shopId,
    //         @RequestBody GhnServiceSelectionDTO selection) {
    //     try {
    //         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //         com.PBL6.Ecommerce.domain.User user = userService.resolveCurrentUser(authentication);

    //         com.PBL6.Ecommerce.domain.Shop shop = shopService.getShopByIdAndOwner(shopId, user);

    //         // Validate selection against GHN available services if possible
    //         Map<String,Object> checkPayload = new HashMap<>();
    //         // Try to populate using shop pickup address (GhnService will do it)
    //         Map<String,Object> availResp = ghnService.getAvailableServices(checkPayload, shopId);

    //         boolean valid = false;
    //         try {
    //                 Object data = availResp.get("data");
    //                 if (data instanceof Map) {
    //                     @SuppressWarnings("unchecked")
    //                     Map<String, Object> dataMap = (Map<String, Object>) data;
    //                     Object services = dataMap.get("services");
    //                     if (services instanceof Iterable) {
    //                         for (Object sObj : (Iterable<?>) services) {
    //                             if (sObj instanceof Map) {
    //                                 @SuppressWarnings("unchecked")
    //                                 Map<String, Object> s = (Map<String, Object>) sObj;
    //                                 Object sid = s.get("service_id");
    //                                 Object stid = s.get("service_type_id");
    //                                 if (sid != null && stid != null) {
    //                                     try {
    //                                         int sidI = Integer.parseInt(String.valueOf(sid));
    //                                         int stidI = Integer.parseInt(String.valueOf(stid));
    //                                         if (selection.getServiceId() != null && selection.getServiceTypeId() != null
    //                                                 && sidI == selection.getServiceId() && stidI == selection.getServiceTypeId()) {
    //                                             valid = true; break;
    //                                         }
    //                                     } catch (Exception ignored) {}
    //                                 }
    //                             }
    //                         }
    //                     }
    //                 }
    //         } catch (Exception ignored) {}

    //         if (!valid) {
    //             // If we couldn't validate (unknown format) allow save but warn in message
    //             // Otherwise, if validation failed explicitly, return 400
    //             // For now, allow save but include warning in response message
    //         }

    //         shop.setGhnServiceId(selection.getServiceId());
    //         shop.setGhnServiceTypeId(selection.getServiceTypeId());
    //         Shop saved = shopService.saveShop(shop);
    //         com.PBL6.Ecommerce.domain.dto.ShopDTO dto = shopService.toDTO(saved);

    //         String msg = valid ? "Lưu dịch vụ GHN thành công" : "Lưu dịch vụ GHN (không kiểm chứng bởi GHN)";
    //         return ResponseEntity.ok(new ResponseDTO<>(200, null, msg, dto));
    //     } catch (RuntimeException e) {
    //         String msg = e.getMessage();
    //         int code = msg != null && msg.contains("không có quyền") ? 403 : 400;
    //         return ResponseEntity.status(code).body(new ResponseDTO<>(code, msg, "Lưu thất bại", null));
    //     } catch (Exception ex) {
    //         return ResponseEntity.status(500).body(new ResponseDTO<>(500, ex.getMessage(), "Lỗi hệ thống", null));
    //     }
    // }

    /**
     * API lấy thông tin shop của seller
     * GET /api/seller/shop
     * Tự động lấy shop theo seller đang đăng nhập
     * Chỉ SELLER mới có quyền truy cập
     */
    @GetMapping("/seller/shop")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<ShopDTO>> getShop() {
        try {
            // Lấy thông tin user từ JWT token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Lấy thông tin shop
            ShopDTO shop = shopService.getSellerShop(username);

            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Lấy thông tin shop thành công", shop)
            );
        } catch (RuntimeException e) {
            // Phân biệt loại lỗi
            String errorMessage = e.getMessage();
            int statusCode;

            if (errorMessage.contains("chưa có shop")) {
                statusCode = 404;
            } else if (errorMessage.contains("không phải là seller")) {
                statusCode = 403;
            } else {
                statusCode = 400;
            }

            return ResponseEntity.status(statusCode).body(
                new ResponseDTO<>(statusCode, errorMessage, "Lấy thông tin shop thất bại", null)
            );
        }
    }

    // /**
    //  * Kết nối shop với GHN (đăng ký shop trên GHN)
    //  * POST /api/shops/{shopId}/connect-ghn
    //  * Chỉ owner (SELLER) mới được phép
    //  */
    // @PostMapping("/shops/{shopId}/connect-ghn")
    // @PreAuthorize("hasRole('SELLER')")
    // public ResponseEntity<ResponseDTO<Map<String, Object>>> connectGhn(@PathVariable Long shopId) {
    //     try {
    //         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //         com.PBL6.Ecommerce.domain.User user = userService.resolveCurrentUser(authentication);

    //         // ensure ownership
    //         com.PBL6.Ecommerce.domain.Shop shop = shopService.getShopByIdAndOwner(shopId, user);

    //         // build payload from shop (use pickupAddress if available)
    //         Map<String, Object> payload = new HashMap<>();
    //         payload.put("shop_name", shop.getName());
    //         payload.put("phone", user.getPhoneNumber());
    //         if (shop.getPickupAddress() != null) {
    //             var pa = shop.getPickupAddress();
    //             payload.put("shop_address", pa.getFullAddress());
    //             if (pa.getDistrictId() != null) payload.put("from_district_id", pa.getDistrictId());
    //             if (pa.getWardCode() != null) payload.put("from_ward_code", pa.getWardCode());
    //         }

    //         // try to create GHN shop synchronously (best-effort)
    //         String externalGhnId = ghnService.createGhnShop(payload);
    //         if (externalGhnId == null) {
    //             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
    //                 new ResponseDTO<>(500, "GHN kết nối thất bại", "Kết nối GHN thất bại", null)
    //             );
    //         }

    //         // save GHN id to shop
    //         shop.setGhnShopId(externalGhnId);
    //         shopService.saveShop(shop);

    //         Map<String, Object> result = new HashMap<>();
    //         result.put("ghnShopId", externalGhnId);

    //         return ResponseEntity.ok(new ResponseDTO<>(200, null, "Kết nối GHN thành công", result));

    //     } catch (RuntimeException e) {
    //         String msg = e.getMessage();
    //         int code = msg != null && msg.contains("không có quyền") ? 403 : 400;
    //         return ResponseEntity.status(code).body(new ResponseDTO<>(code, msg, "Kết nối GHN thất bại", null));
    //     } catch (Exception ex) {
    //         return ResponseEntity.status(500).body(new ResponseDTO<>(500, ex.getMessage(), "Lỗi hệ thống", null));
    //     }
    // }

    /**
     * Cập nhật GHN credentials cho 1 shop (token, service ids, external shop id)
     * PUT /api/shops/{shopId}/ghn-credentials
     */
    @PutMapping("/shops/{shopId}/ghn-credentials")
    @PreAuthorize("hasRole('SELLER')")
    @Transactional
    public ResponseEntity<ResponseDTO<Shop>> updateGhnCredentials(
            @PathVariable Long shopId,
            @Valid @RequestBody GhnCredentialsDTO dto,
            Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            Long userId = Long.valueOf(jwt.getSubject());
            
            // Gọi qua ShopService thay vì ShopRepository
            Shop updated = shopService.updateGhnCredentials(shopId, userId, dto);
            
            return ResponseDTO.ok(updated, "Cập nhật GHN credentials thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ResponseDTO<>(400, e.getMessage(), "Lỗi", null));
        }
    }

    /**
     * API cập nhật thông tin shop
     * PUT /api/seller/shop
     * Cập nhật: name, address, description, status
     * Chỉ SELLER mới có quyền và chỉ cập nhật được shop của mình
     */
    @PutMapping("/seller/shop")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<ShopDTO>> updateShop(@RequestBody UpdateShopDTO updateShopDTO) {
        try {
            // Lấy thông tin user từ JWT token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Cập nhật thông tin shop
            ShopDTO updatedShop = shopService.updateSellerShop(username, updateShopDTO);

            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Cập nhật thông tin shop thành công", updatedShop)
            );
        } catch (RuntimeException e) {
            // Phân biệt loại lỗi
            String errorMessage = e.getMessage();
            int statusCode;

            if (errorMessage.contains("chưa có shop")) {
                statusCode = 404;
            } else if (errorMessage.contains("không phải là seller")) {
                statusCode = 403;
            } else if (errorMessage.contains("không hợp lệ")) {
                statusCode = 400;
            } else {
                statusCode = 400;
            }

            return ResponseEntity.status(statusCode).body(
                new ResponseDTO<>(statusCode, errorMessage, "Cập nhật thông tin shop thất bại", null)
            );
        }
    }

    /**
     * API lấy thống kê thu nhập của shop
     * GET /api/seller/shop/analytics
     * Thống kê doanh thu theo tháng (chỉ đơn COMPLETED)
     * Chỉ SELLER mới có quyền truy cập
     * 
     * @param year - Năm cần thống kê (optional, mặc định là năm hiện tại)
     * @return ShopAnalyticsDTO - Tổng doanh thu, số đơn hàng, doanh thu theo tháng
     */
    @GetMapping("/seller/shop/analytics")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<ShopAnalyticsDTO>> getShopAnalytics(
            @RequestParam(required = false) Integer year) {
        try {
            // Lấy thông tin user từ JWT token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Lấy thống kê shop
            ShopAnalyticsDTO analytics = shopService.getShopAnalytics(username, year);

            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Lấy thống kê shop thành công", analytics)
            );
        } catch (RuntimeException e) {
            // Phân biệt loại lỗi
            String errorMessage = e.getMessage();
            int statusCode;

            if (errorMessage.contains("chưa có shop")) {
                statusCode = 404;
            } else if (errorMessage.contains("không phải là seller")) {
                statusCode = 403;
            } else {
                statusCode = 400;
            }

            return ResponseEntity.status(statusCode).body(
                new ResponseDTO<>(statusCode, errorMessage, "Lấy thống kê shop thất bại", null)
            );
        }
    }
    /**
     * Đăng ký seller (Buyer upgrade to Seller) - sử dụng ShopRegistrationDTO duy nhất
     * POST /api/seller/register
     */
    @PostMapping("/seller/register")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ResponseDTO<ShopDTO>> registerAsSeller(
            @Valid @RequestBody ShopRegistrationDTO registrationDTO) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            com.PBL6.Ecommerce.domain.User user = userService.resolveCurrentUser(authentication);

            // Create shop and upgrade to seller (auto-approval)
            Shop shop = shopService.createShopFromSellerRegistration(user, registrationDTO);

            ShopDTO dto = shopService.toDTO(shop);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseDTO<>(201, null, "Đăng ký seller thành công! Role đã được nâng cấp.", dto)
            );

        } catch (RuntimeException e) {
            String errorMessage = e.getMessage();
            int statusCode;

            if (errorMessage.contains("Chỉ BUYER")) {
                statusCode = 403;
            } else if (errorMessage.contains("đã tồn tại") || errorMessage.contains("đã có shop")) {
                statusCode = 409;
            } else {
                statusCode = 400;
            }

            return ResponseEntity.status(statusCode).body(
                new ResponseDTO<>(statusCode, errorMessage, "Đăng ký seller thất bại", null)
            );
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new ResponseDTO<>(500, ex.getMessage(), "Lỗi hệ thống", null));
        }
    }
    
    @GetMapping("/shops/user/{userId}")
    public ResponseEntity<ResponseDTO<Shop>> getShopByUserId(@PathVariable Long userId) {
        try {
            Shop shop = shopService.getShopByUserId(userId);
            if (shop != null) {
                ResponseDTO<Shop> response = new ResponseDTO<>(
                    HttpStatus.OK.value(),
                    null,
                    "Lấy thông tin shop thành công",
                    shop
                );
                return ResponseEntity.ok(response);
            } else {
                ResponseDTO<Shop> response = new ResponseDTO<>(
                    HttpStatus.NOT_FOUND.value(),
                    "Người dùng chưa có shop",
                    "Thất bại",
                    null
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            ResponseDTO<Shop> response = new ResponseDTO<>(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage(),
                "Thất bại",
                null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/shops/check/{userId}")
    public ResponseEntity<ResponseDTO<Boolean>> checkUserHasShop(@PathVariable Long userId) {
        try {
            boolean hasShop = shopService.hasShop(userId);
            ResponseDTO<Boolean> response = new ResponseDTO<>(
                HttpStatus.OK.value(),
                null,
                "Kiểm tra thành công",
                hasShop
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<Boolean> response = new ResponseDTO<>(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage(),
                "Thất bại",
                false
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
}