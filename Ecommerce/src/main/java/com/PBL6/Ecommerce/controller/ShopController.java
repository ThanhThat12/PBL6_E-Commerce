package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.ShopDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateShopDTO;
import com.PBL6.Ecommerce.domain.dto.ShopAnalyticsDTO;
import com.PBL6.Ecommerce.service.ShopService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.ShopRegistrationDTO;
import com.PBL6.Ecommerce.service.ShopService;
import com.PBL6.Ecommerce.service.UserService;

@RestController
@RequestMapping("/api")
public class ShopController {
    
    @Autowired
    private ShopService shopService;
     @Autowired
    private UserService userService;
    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

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
    @PostMapping("/shops/register")
public ResponseEntity<ResponseDTO<Shop>> registerShop(
        @Valid @RequestBody ShopRegistrationDTO shopRegistrationDTO) {
    try {
        // Sử dụng getCurrentUser() có sẵn trong UserService
        Long userId = userService.getCurrentUser().getId();
        
        Shop shop = shopService.registerShop(userId, shopRegistrationDTO);
        ResponseDTO<Shop> response = new ResponseDTO<>(
            HttpStatus.CREATED.value(),
            null,
            "Đăng ký shop thành công",
            shop
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
