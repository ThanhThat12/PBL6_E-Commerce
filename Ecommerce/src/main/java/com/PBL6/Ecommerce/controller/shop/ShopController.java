package com.PBL6.Ecommerce.controller.shop;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.entity.shop.Shop;
import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.domain.dto.GhnCredentialsDTO;
import com.PBL6.Ecommerce.domain.dto.RegistrationStatusDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.SellerRegistrationRequestDTO;
import com.PBL6.Ecommerce.domain.dto.SellerRegistrationResponseDTO;
import com.PBL6.Ecommerce.domain.dto.ShopAnalyticsDTO;
import com.PBL6.Ecommerce.domain.dto.ShopDTO;
import com.PBL6.Ecommerce.domain.dto.ShopDetailDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateShopDTO;
import com.PBL6.Ecommerce.service.GhnService;
import com.PBL6.Ecommerce.service.SellerRegistrationService;
import com.PBL6.Ecommerce.service.ShopService;
import com.PBL6.Ecommerce.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Tag(name = "Shop Management", description = "APIs for shop profile, seller registration, and shop analytics")
public class ShopController {
    
    private final ShopService shopService;
    private final UserService userService;
    private final GhnService ghnService;
    private final SellerRegistrationService sellerRegistrationService;
    
    public ShopController(ShopService shopService, UserService userService, GhnService ghnService,
                          SellerRegistrationService sellerRegistrationService) {
        this.shopService = shopService;
        this.userService = userService;
        this.ghnService = ghnService;
        this.sellerRegistrationService = sellerRegistrationService;
    }

    /**
     * API lấy thông tin shop của seller
     * GET /api/seller/shop
     * Tự động lấy shop theo seller đang đăng nhập
     * Chỉ SELLER mới có quyền truy cập
     */
    @Operation(
        summary = "Get seller's shop information",
        description = "Get basic shop information for the currently authenticated seller",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved shop information",
            content = @Content(schema = @Schema(implementation = ShopDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User is not a seller"),
        @ApiResponse(responseCode = "404", description = "Shop not found for this seller")
    })
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
     * API lấy thông tin shop CHI TIẾT của seller (bao gồm GHN, KYC, address, owner info)
     * GET /api/seller/shop/detail
     * Trả về ShopDetailDTO với đầy đủ thông tin để frontend hiển thị và cho phép chỉnh sửa
     * Chỉ SELLER mới có quyền truy cập
     */
    @GetMapping("/seller/shop/detail")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<ShopDetailDTO>> getShopDetail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            ShopDetailDTO shopDetail = shopService.getSellerShopDetail(username);

            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Lấy thông tin chi tiết shop thành công", shopDetail)
            );
        } catch (RuntimeException e) {
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
     * Cập nhật: name, description, contact, branding, address, GHN credentials, status
     * Chỉ SELLER mới có quyền và chỉ cập nhật được shop của mình
     * Trả về ShopDetailDTO với đầy đủ thông tin sau khi cập nhật
     */
    @PutMapping("/seller/shop")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<ShopDetailDTO>> updateShop(@RequestBody UpdateShopDTO updateShopDTO) {
        try {
            // Lấy thông tin user từ JWT token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Cập nhật thông tin shop
            ShopDetailDTO updatedShop = shopService.updateSellerShop(username, updateShopDTO);

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
        } catch (NumberFormatException e) {
            // Handle invalid year parameter (e.g., year=month)
            System.err.println("⚠️ Invalid year parameter: " + e.getMessage());
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, "Invalid year parameter", "Lấy thống kê shop thất bại", null)
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
     * Submit seller registration (BUYER applies to become SELLER)
     * POST /api/registration/submit
     * Creates PENDING shop, requires admin approval for role transition
     * 
     * NOTE: User remains BUYER role until admin APPROVES. After approval,
     * role automatically changes to SELLER and user can access /api/seller/** endpoints.
     */
    @Operation(
        summary = "Submit seller registration application",
        description = "Allows BUYER to apply for seller status. Creates a PENDING shop that requires admin approval. " +
                      "After approval, user role automatically changes to SELLER.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Registration submitted successfully",
            content = @Content(schema = @Schema(implementation = SellerRegistrationResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid registration data"),
        @ApiResponse(responseCode = "403", description = "User is not a BUYER or already has active registration"),
        @ApiResponse(responseCode = "409", description = "Duplicate shop name or existing PENDING registration")
    })
    @PostMapping("/seller/register")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ResponseDTO<SellerRegistrationResponseDTO>> submitSellerRegistration(
            @Valid @RequestBody SellerRegistrationRequestDTO registrationDTO) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.resolveCurrentUser(authentication);

            // Submit registration (creates PENDING shop)
            // Role transition (BUYER → SELLER) happens when admin APPROVES (in AdminController)
            SellerRegistrationResponseDTO response = sellerRegistrationService.submitRegistration(user, registrationDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseDTO<>(201, null, response.getMessage(), response)
            );

        } catch (RuntimeException e) {
            String errorMessage = e.getMessage();
            int statusCode;

            if (errorMessage.contains("không phải BUYER") || errorMessage.contains("Chỉ tài khoản BUYER")) {
                statusCode = 403;
            } else if (errorMessage.contains("đã có đơn đăng ký") || errorMessage.contains("đã có shop")) {
                statusCode = 409;  // Conflict: user already has pending/active registration
            } else if (errorMessage.contains("Tên shop đã tồn tại")) {
                statusCode = 409;  // Conflict: shop name duplicate
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

    /**
     * Get registration status for current user
     * GET /api/registration/status
     * Returns registration status: PENDING, APPROVED, REJECTED, or NOT_FOUND
     * Accessible to both BUYER and SELLER (checks both new applications and approved shops)
     */
    @Operation(
        summary = "Get seller registration status",
        description = "Get the current registration status for the authenticated user (BUYER checking application or SELLER checking approval status)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registration status retrieved successfully",
            content = @Content(schema = @Schema(implementation = RegistrationStatusDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "No registration found for this user")
    })
    @GetMapping("/seller/registration/status")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<ResponseDTO<RegistrationStatusDTO>> getRegistrationStatus() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.resolveCurrentUser(authentication);

            RegistrationStatusDTO status = sellerRegistrationService.getRegistrationStatus(user);

            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Lấy trạng thái đăng ký thành công", status)
            );

        } catch (RuntimeException e) {
            String errorMessage = e.getMessage();
            int statusCode = errorMessage.contains("chưa có đơn đăng ký") ? 404 : 400;

            return ResponseEntity.status(statusCode).body(
                new ResponseDTO<>(statusCode, errorMessage, "Lấy trạng thái thất bại", null)
            );
        }
    }

    /**
     * Cancel registration application
     * DELETE /api/registration
     * Allows BUYER to cancel PENDING application (before admin review)
     * Allows SELLER to cancel REJECTED application (and submit new one)
     * After cancellation, new registration can be submitted immediately
     */
    @Operation(
        summary = "Cancel seller registration application",
        description = "Cancel PENDING or REJECTED registration. After cancellation, new registration can be submitted.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registration cancelled successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "No cancellable registration found (PENDING or REJECTED)")
    })
    @DeleteMapping("/seller/registration")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<ResponseDTO<Boolean>> cancelRejectedApplication() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.resolveCurrentUser(authentication);

            // Allows cancelling PENDING (BUYER) or REJECTED (SELLER) registrations
            boolean cancelled = sellerRegistrationService.cancelRejectedApplication(user);

            if (cancelled) {
                return ResponseEntity.ok(
                    new ResponseDTO<>(200, null, "Đã hủy đơn đăng ký. Bạn có thể đăng ký lại.", true)
                );
            } else {
                return ResponseEntity.status(404).body(
                    new ResponseDTO<>(404, "Không tìm thấy đơn đăng ký đang chờ hoặc bị từ chối để hủy", "Thất bại", false)
                );
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ResponseDTO<>(500, e.getMessage(), "Lỗi hệ thống", false)
            );
        }
    }
    /**
     * Update rejected application with new information
     * PUT /api/registration
     * Allows BUYER to fix issues on REJECTED application and resubmit as PENDING
     * Status changes back to PENDING for admin review (approval cycle repeats)
     * 
     * IMPORTANT: Only works if current registration status is REJECTED.
     * Cannot update PENDING or APPROVED registrations.
     */
    @Operation(
        summary = "Update rejected seller registration",
        description = "Update and resubmit REJECTED registration with new information. Status changes back to PENDING for admin review. " +
                      "Can only update registrations that are in REJECTED state.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registration updated and resubmitted",
            content = @Content(schema = @Schema(implementation = SellerRegistrationResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid data or registration not in REJECTED state"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "No REJECTED registration found to update")
    })
    @PutMapping("/seller/registration")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ResponseDTO<SellerRegistrationResponseDTO>> updateRejectedApplication(
            @Valid @RequestBody SellerRegistrationRequestDTO request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.resolveCurrentUser(authentication);

            // Only updates REJECTED registrations (resets status to PENDING)
            SellerRegistrationResponseDTO response = sellerRegistrationService.updateRejectedApplication(user, request);

            return ResponseEntity.ok(
                    new ResponseDTO<>(200, null, response.getMessage(), response)
            );

        } catch (RuntimeException e) {
            String errorMessage = e.getMessage();
            int statusCode;
            
            if (errorMessage.contains("không phải REJECTED") || errorMessage.contains("không có") || 
                errorMessage.contains("không tìm thấy")) {
                // Not a REJECTED registration or doesn't exist
                statusCode = 404;
            } else if (errorMessage.contains("PENDING") || errorMessage.contains("APPROVED")) {
                // Already PENDING or APPROVED
                statusCode = 400;
            } else {
                statusCode = 400;
            }
            
            return ResponseEntity.status(statusCode).body(
                    new ResponseDTO<>(statusCode, errorMessage, "Cập nhật đơn đăng ký thất bại", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    new ResponseDTO<>(500, e.getMessage(), "Lỗi hệ thống", null)
            );
        }
    }

    /**
     * Check if current user can submit new seller registration
     * GET /api/registration/can-submit
     * Returns true if user (BUYER) is eligible to submit registration
     * Returns false if user already has pending/approved registration or active shop
     */
    @Operation(
        summary = "Check if user can submit seller registration",
        description = "Verify if the current BUYER user is eligible to submit a new seller registration. " +
                      "Returns false if user already has PENDING/APPROVED registration or active SELLER role.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Eligibility check completed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/seller/registration/can-submit")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> canSubmitRegistration() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.resolveCurrentUser(authentication);

            boolean canSubmit = sellerRegistrationService.canSubmitRegistration(user);

            Map<String, Object> result = new HashMap<>();
            result.put("canSubmit", canSubmit);
            result.put("message", canSubmit 
                ? "Bạn có thể đăng ký bán hàng" 
                : "Bạn đã có đơn đăng ký đang chờ duyệt hoặc shop đang hoạt động");

            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Kiểm tra thành công", result)
            );

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ResponseDTO<>(500, e.getMessage(), "Lỗi hệ thống", null)
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
     * Get shop by ID (public endpoint)
     * GET /api/shops/{shopId}
     */
    @GetMapping("/shops/{shopId}")
    public ResponseEntity<ResponseDTO<ShopDTO>> getShopById(@PathVariable Long shopId) {
        try {
            Shop shop = shopService.getShopById(shopId);
            if (shop != null) {
                ShopDTO dto = shopService.toDTO(shop);
                return ResponseEntity.ok(
                    new ResponseDTO<>(HttpStatus.OK.value(), null, "Lấy thông tin shop thành công", dto)
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseDTO<>(HttpStatus.NOT_FOUND.value(), "Shop không tồn tại", "Thất bại", null)
                );
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseDTO<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), "Thất bại", null)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), "Thất bại", null)
            );
        }
    }
}