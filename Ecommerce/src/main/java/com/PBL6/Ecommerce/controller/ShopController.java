package com.PBL6.Ecommerce.controller;

/*
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
@RequestMapping("/api/shops")
public class ShopController {
    
    @Autowired
    private ShopService shopService;
     @Autowired
    private UserService userService;
    
    @PostMapping("/register")
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
    
    @GetMapping("/user/{userId}")
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
    
    @GetMapping("/check/{userId}")
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
*/