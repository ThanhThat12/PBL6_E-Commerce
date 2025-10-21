package com.PBL6.Ecommerce.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.PBL6.Ecommerce.domain.dto.ShopDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.service.ShopService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/shops")
public class AdminShopController {
    private final ShopService shopService;

    public AdminShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<ResponseDTO<List<ShopDTO>>> listShops() {
        try {
            List<ShopDTO> shops = shopService.listAllShops();
            ResponseDTO<List<ShopDTO>> response = new ResponseDTO<>(200, null, "Lấy danh sách shop thành công", shops);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<List<ShopDTO>> response = new ResponseDTO<>(400, e.getMessage(), "Thất bại", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<ShopDTO>> getShop(@PathVariable Long id) {
        try {
            ShopDTO shop = shopService.getShopById(id);
            if (shop == null) {
                ResponseDTO<ShopDTO> response = new ResponseDTO<>(404, "NOT_FOUND", "Không tìm thấy shop", null);
                return ResponseEntity.status(404).body(response);
            }
            ResponseDTO<ShopDTO> response = new ResponseDTO<>(200, null, "Lấy chi tiết shop thành công", shop);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<ShopDTO> response = new ResponseDTO<>(400, e.getMessage(), "Thất bại", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ResponseDTO<ShopDTO>> approve(@PathVariable Long id) {
        try {
            ShopDTO shop = shopService.approveShop(id);
            if (shop == null) {
                ResponseDTO<ShopDTO> response = new ResponseDTO<>(404, "NOT_FOUND", "Không tìm thấy shop", null);
                return ResponseEntity.status(404).body(response);
            }
            ResponseDTO<ShopDTO> response = new ResponseDTO<>(200, null, "Duyệt shop thành công", shop);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<ShopDTO> response = new ResponseDTO<>(400, e.getMessage(), "Thất bại", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ResponseDTO<ShopDTO>> reject(@PathVariable Long id) {
        try {
            ShopDTO shop = shopService.rejectShop(id);
            if (shop == null) {
                ResponseDTO<ShopDTO> response = new ResponseDTO<>(404, "NOT_FOUND", "Không tìm thấy shop", null);
                return ResponseEntity.status(404).body(response);
            }
            ResponseDTO<ShopDTO> response = new ResponseDTO<>(200, null, "Từ chối shop thành công", shop);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<ShopDTO> response = new ResponseDTO<>(400, e.getMessage(), "Thất bại", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PatchMapping("/{id}/suspend")
    public ResponseEntity<ResponseDTO<ShopDTO>> suspend(@PathVariable Long id) {
        try {
            ShopDTO shop = shopService.suspendShop(id);
            if (shop == null) {
                ResponseDTO<ShopDTO> response = new ResponseDTO<>(404, "NOT_FOUND", "Không tìm thấy shop", null);
                return ResponseEntity.status(404).body(response);
            }
            ResponseDTO<ShopDTO> response = new ResponseDTO<>(200, null, "Tạm ngưng shop thành công", shop);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<ShopDTO> response = new ResponseDTO<>(400, e.getMessage(), "Thất bại", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<ResponseDTO<ShopDTO>> verify(@PathVariable Long id) {
        try {
            ShopDTO shop = shopService.verifyShop(id);
            if (shop == null) {
                ResponseDTO<ShopDTO> response = new ResponseDTO<>(404, "NOT_FOUND", "Không tìm thấy shop", null);
                return ResponseEntity.status(404).body(response);
            }
            ResponseDTO<ShopDTO> response = new ResponseDTO<>(200, null, "Xác minh shop thành công", shop);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<ShopDTO> response = new ResponseDTO<>(400, e.getMessage(), "Thất bại", null);
            return ResponseEntity.badRequest().body(response);
        }
    }
}