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
import org.springframework.web.multipart.MultipartFile;

import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.ShopAnalyticsDTO;
import com.PBL6.Ecommerce.domain.dto.ShopDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateShopDTO;
import com.PBL6.Ecommerce.dto.seller.ImageUploadResponseDTO;
import com.PBL6.Ecommerce.dto.seller.ShopStatsDTO;
import com.PBL6.Ecommerce.service.CloudinaryService;
import com.PBL6.Ecommerce.service.ShopService;
import com.PBL6.Ecommerce.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ShopController {
    
    private final ShopService shopService;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;
    
    public ShopController(ShopService shopService, UserService userService, CloudinaryService cloudinaryService) {
        this.shopService = shopService;
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
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
    /**
     * Đăng ký seller (Shopee-style: Buyer upgrade to Seller)
     * POST /api/seller/register
     * 
     * Requirements:
     * - Must be BUYER role
     * - Must not have existing shop
     * - Auto-approved for simplicity (student project)
     * 
     * @param registrationDTO Shop registration data
     * @return Shop creation response
     */
    @PostMapping("/seller/register")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ResponseDTO<com.PBL6.Ecommerce.dto.seller.SellerRegistrationResponseDTO>> registerAsSeller(
            @Valid @RequestBody com.PBL6.Ecommerce.dto.seller.SellerRegistrationDTO registrationDTO) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            com.PBL6.Ecommerce.domain.User user = userService.resolveCurrentUser(authentication);
            
            // Create shop and upgrade to seller (auto-approval)
            Shop shop = shopService.createShopFromSellerRegistration(user, registrationDTO);
            
            // Build success response
            com.PBL6.Ecommerce.dto.seller.SellerRegistrationResponseDTO response = 
                com.PBL6.Ecommerce.dto.seller.SellerRegistrationResponseDTO.success(
                    shop.getId(),
                    shop.getName()
                );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseDTO<>(201, null, "Đăng ký seller thành công! Role đã được nâng cấp.", response)
            );
            
        } catch (RuntimeException e) {
            // Handle business logic errors
            String errorMessage = e.getMessage();
            int statusCode;
            
            if (errorMessage.contains("Chỉ BUYER")) {
                statusCode = 403; // Forbidden
            } else if (errorMessage.contains("đã tồn tại") || errorMessage.contains("đã có shop")) {
                statusCode = 409; // Conflict
            } else {
                statusCode = 400; // Bad Request
            }
            
            return ResponseEntity.status(statusCode).body(
                new ResponseDTO<>(statusCode, errorMessage, "Đăng ký seller thất bại", null)
            );
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

    /**
     * API l?y th?ng k� shop
     * GET /api/seller/shop/stats
     */
    @GetMapping("/seller/shop/stats")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<ShopStatsDTO>> getShopStats() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            ShopStatsDTO stats = shopService.getShopStats(username);
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "L?y th?ng k� shop th�nh c�ng", stats));
        } catch (RuntimeException e) {
            int statusCode = e.getMessage().contains("chua c� shop") ? 404 : 400;
            return ResponseEntity.status(statusCode).body(
                new ResponseDTO<>(statusCode, e.getMessage(), "L?y th?ng k� shop th?t b?i", null)
            );
        }
    }
    
    /**
     * API upload logo shop
     * POST /api/seller/shop/logo
     */
    @PostMapping("/seller/shop/logo")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<ImageUploadResponseDTO>> uploadLogo(
            @RequestParam("logo") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // Get seller ID from shop
            ShopDTO shopDTO = shopService.getSellerShop(username);
            
            // Upload to Cloudinary
            String imageUrl = cloudinaryService.uploadShopLogo(file, shopDTO.getId());
            
            // TODO: Optionally update shop.logoUrl in database
            // shopService.updateShopLogo(shopDTO.getId(), imageUrl);
            
            ImageUploadResponseDTO response = new ImageUploadResponseDTO(imageUrl, "Logo uploaded successfully");
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "Upload logo thành công", response));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ResponseDTO<>(500, e.getMessage(), "Upload logo thất bại", null)
            );
        }
    }
    
    /**
     * API upload banner shop
     * POST /api/seller/shop/banner
     */
    @PostMapping("/seller/shop/banner")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<ImageUploadResponseDTO>> uploadBanner(
            @RequestParam("banner") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // Get seller ID from shop
            ShopDTO shopDTO = shopService.getSellerShop(username);
            
            // Upload to Cloudinary
            String imageUrl = cloudinaryService.uploadShopBanner(file, shopDTO.getId());
            
            // TODO: Optionally update shop.bannerUrl in database
            // shopService.updateShopBanner(shopDTO.getId(), imageUrl);
            
            ImageUploadResponseDTO response = new ImageUploadResponseDTO(imageUrl, "Banner uploaded successfully");
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "Upload banner thành công", response));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ResponseDTO<>(500, e.getMessage(), "Upload banner thất bại", null)
            );
        }
    }
}
