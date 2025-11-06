package com.PBL6.Ecommerce.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import com.PBL6.Ecommerce.domain.dto.CheckContactDTO;
import com.PBL6.Ecommerce.domain.dto.RegisterDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.TopBuyerDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateUserRoleDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateUserStatusDTO;
import com.PBL6.Ecommerce.domain.dto.UserInfoDTO;
import com.PBL6.Ecommerce.domain.dto.VerifyOtpDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminUserDetailDTO;
import com.PBL6.Ecommerce.domain.dto.admin.CustomerStatsDTO;
import com.PBL6.Ecommerce.domain.dto.admin.ListCustomerUserDTO;
import com.PBL6.Ecommerce.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/register/check-contact")
    public ResponseEntity<ResponseDTO<String>> checkContact(@RequestBody CheckContactDTO dto) {
        String result = userService.checkContact(dto);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Success", result));
    }

    @PostMapping("/register/verify-otp")
    public ResponseEntity<ResponseDTO<String>> verifyOtp(@RequestBody VerifyOtpDTO dto) {
        String result = userService.verifyOtp(dto);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Success", result));
    }

    @PostMapping("/register/register")
    public ResponseEntity<ResponseDTO<String>> register(@RequestBody RegisterDTO dto) {
        String result = userService.register(dto);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Success", result));
    }
    
    @GetMapping("/user/me")
    public ResponseEntity<ResponseDTO<UserInfoDTO>> getCurrentUser() {
        UserInfoDTO userInfo = userService.getCurrentUser();
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "User info retrieved successfully", userInfo));
    }



    // ================================
    // ADMIN APIs
    // ================================
    // Admin APIs - Chỉ admin mới có thể truy cập
    @GetMapping("/admin/users/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<UserInfoDTO>>> getAdminUsers() {
        List<UserInfoDTO> adminUsers = userService.getUsersByRole("ADMIN");
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Admin users retrieved successfully", adminUsers));
    }


    @GetMapping("/admin/users/sellers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<UserInfoDTO>>> getSellerUsers() {
        List<UserInfoDTO> sellerUsers = userService.getUsersByRole("SELLER");
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Seller users retrieved successfully", sellerUsers));
    }

    @GetMapping("/admin/users/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<ListCustomerUserDTO>>> getCustomerUsers() {
        List<ListCustomerUserDTO> customerUsers = userService.getCustomerUsers();
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Customer users retrieved successfully", customerUsers));
    }
    
    @GetMapping("/admin/users/customers/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<CustomerStatsDTO>> getCustomerStats() {
        CustomerStatsDTO stats = userService.getCustomerStats();
        return ResponseEntity.ok(
            new ResponseDTO<>(200, null, "Customer statistics retrieved successfully", stats)
        );
    }
    // API lấy chi tiết 1 user theo ID
    @GetMapping("/admin/users/detail/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminUserDetailDTO>> getUserDetail(@PathVariable Long userId) {
        AdminUserDetailDTO userDetail = userService.getUserDetailById(userId);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "User detail retrieved successfully", userDetail));
    }

    // API thay đổi role của user
    @PatchMapping("/admin/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<UserInfoDTO>> updateUserRole(
            @PathVariable Long userId,
            @RequestBody UpdateUserRoleDTO dto) {
        UserInfoDTO updatedUser = userService.updateUserRole(userId, dto.getRole());
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "User role updated successfully", updatedUser));
    }

    // API thay đổi trạng thái user (active/inactive)
    @PatchMapping("/admin/users/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<UserInfoDTO>> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UpdateUserStatusDTO dto) {
        UserInfoDTO updatedUser = userService.updateUserStatus(userId, dto.isActivated());
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "User status updated successfully", updatedUser));
    }
    
    // API xóa user http://127.0.0.1:8081/api/admin/users/{userId}
    @DeleteMapping("/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<String>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "User deleted successfully", "User has been removed from the system"));
    }

    // ================================
    // TOP BUYERS APIs
    // ================================

    /**
     * API lấy danh sách tất cả top buyers (cho ADMIN)
     * GET /api/admin/users/top-buyers
     */
    @GetMapping("/admin/users/top-buyers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<TopBuyerDTO>>> getAllTopBuyers() {
        try {
            List<TopBuyerDTO> topBuyers = userService.getAllTopBuyers();
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Lấy danh sách top buyers thành công", topBuyers)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Lấy danh sách top buyers thất bại", null)
            );
        }
    }

    /**
     * API lấy top buyers với phân trang (cho ADMIN)
     * GET /api/admin/users/top-buyers/page?page=0&size=10
     */
    @GetMapping("/admin/users/top-buyers/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<TopBuyerDTO>>> getTopBuyersWithPaging(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<TopBuyerDTO> topBuyers = userService.getAllTopBuyers(pageable);
            
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Lấy danh sách top buyers thành công", topBuyers)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Lấy danh sách top buyers thất bại", null)
            );
        }
    }

    /**
     * API lấy top N buyers (cho ADMIN)
     * GET /api/admin/users/top-buyers/limit/{limit}
     * Ví dụ: /api/admin/users/top-buyers/limit/5 → lấy top 5 buyers
     */
    @GetMapping("/admin/users/top-buyers/limit/{limit}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<TopBuyerDTO>>> getTopBuyersWithLimit(
            @PathVariable int limit) {
        try {
            if (limit <= 0 || limit > 100) {
                return ResponseEntity.badRequest().body(
                    new ResponseDTO<>(400, "INVALID_LIMIT", "Limit phải từ 1 đến 100", null)
                );
            }

            List<TopBuyerDTO> topBuyers = userService.getTopBuyers(limit);
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, 
                    String.format("Lấy top %d buyers thành công", limit), topBuyers)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Lấy danh sách top buyers thất bại", null)
            );
        }
    }

    /**
     * API lấy top buyers theo shop ID cụ thể (cho ADMIN)
     * GET /api/admin/users/top-buyers/shop/{shopId}
     * Admin có thể xem top buyers của bất kỳ shop nào
     */
    @GetMapping("/admin/users/top-buyers/shop/{shopId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<TopBuyerDTO>>> getTopBuyersByShopId(@PathVariable Long shopId) {
        try {
            List<TopBuyerDTO> topBuyers = userService.getTopBuyersByShopId(shopId);
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, 
                    String.format("Lấy danh sách top buyers của shop ID %d thành công", shopId), topBuyers)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Lấy danh sách top buyers thất bại", null)
            );
        }
    }

    /**
     * API lấy top buyers của shop với limit (cho ADMIN)
     * GET /api/admin/users/top-buyers/shop/{shopId}/limit/{limit}
     * Admin có thể xem top N buyers của shop cụ thể
     */
    @GetMapping("/admin/users/top-buyers/shop/{shopId}/limit/{limit}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<TopBuyerDTO>>> getTopBuyersByShopIdWithLimit(
            @PathVariable Long shopId, 
            @PathVariable int limit) {
        try {
            if (limit <= 0 || limit > 100) {
                return ResponseEntity.badRequest().body(
                    new ResponseDTO<>(400, "INVALID_LIMIT", "Limit phải từ 1 đến 100", null)
                );
            }

            List<TopBuyerDTO> topBuyers = userService.getTopBuyersByShopIdWithLimit(shopId, limit);
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, 
                    String.format("Lấy top %d buyers của shop ID %d thành công", limit, shopId), topBuyers)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Lấy danh sách top buyers thất bại", null)
            );
        }
    }

    /**
     * API lấy top buyers của shop (cho SELLER)
     * GET /api/seller/top-buyers
     * Chỉ lấy top buyers của shop thuộc seller đang đăng nhập
     */
    @GetMapping("/seller/top-buyers")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<List<TopBuyerDTO>>> getSellerTopBuyers() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            List<TopBuyerDTO> topBuyers = userService.getTopBuyersByShop(username);
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Lấy danh sách top buyers của shop thành công", topBuyers)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Lấy danh sách top buyers thất bại", null)
            );
        }
    }

    /**
     * API lấy top buyers của shop với limit (cho SELLER)
     * GET /api/seller/top-buyers/limit/{limit}
     * Seller chỉ lấy được top N buyers của shop mình
     */
    @GetMapping("/seller/top-buyers/limit/{limit}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<List<TopBuyerDTO>>> getSellerTopBuyersWithLimit(@PathVariable int limit) {
        try {
            if (limit <= 0 || limit > 100) {
                return ResponseEntity.badRequest().body(
                    new ResponseDTO<>(400, "INVALID_LIMIT", "Limit phải từ 1 đến 100", null)
                );
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            List<TopBuyerDTO> topBuyers = userService.getTopBuyersByShopWithLimit(username, limit);
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, 
                    String.format("Lấy top %d buyers của shop thành công", limit), topBuyers)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Lấy danh sách top buyers thất bại", null)
            );
        }
    }
}

