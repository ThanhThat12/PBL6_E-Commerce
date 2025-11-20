package com.PBL6.Ecommerce.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.PBL6.Ecommerce.domain.dto.CheckContactDTO;
import com.PBL6.Ecommerce.domain.dto.RegisterDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.TopBuyerDTO;
import com.PBL6.Ecommerce.domain.dto.UserInfoDTO;
import com.PBL6.Ecommerce.domain.dto.VerifyOtpDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminCreateAdminDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminChangePasswordDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminMyProfileDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminStatsDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminUpdateMyProfileDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminUpdateSellerDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminUpdateUserDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminUserDetailDTO;
import com.PBL6.Ecommerce.domain.dto.admin.CustomerStatsDTO;
import com.PBL6.Ecommerce.domain.dto.admin.ListAdminUserDTO;
import com.PBL6.Ecommerce.domain.dto.admin.ListCustomerUserDTO;
import com.PBL6.Ecommerce.domain.dto.admin.ListSellerUserDTO;
import com.PBL6.Ecommerce.domain.dto.admin.SellerStatsDTO;
import com.PBL6.Ecommerce.service.UserService;



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
    public ResponseEntity<ResponseDTO<List<ListAdminUserDTO>>> getAdminUsers() {
        List<ListAdminUserDTO> adminUsers = userService.getAdminUsers();
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Admin users retrieved successfully", adminUsers));
    }
    
    /**
     * API lấy danh sách admin users với phân trang
     * GET /api/admin/users/admin/page?page=0&size=10
     * @param page - Trang hiện tại (bắt đầu từ 0)
     * @param size - Số lượng items trên mỗi trang (mặc định 10)
     */
    @GetMapping("/admin/users/admin/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<ListAdminUserDTO>>> getAdminUsersWithPaging(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ListAdminUserDTO> adminUsers = userService.getAdminUsers(pageable);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Admin users retrieved successfully", adminUsers));
    }

    @GetMapping("/admin/users/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminStatsDTO>> getAdminStats() {
        AdminStatsDTO stats = userService.getAdminStats();
        return ResponseEntity.ok(
            new ResponseDTO<>(200, null, "Admin statistics retrieved successfully", stats)
        );
    }


    @GetMapping("/admin/users/sellers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<ListSellerUserDTO>>> getSellerUsers(
            @RequestParam(value = "status", required = false) String status) {
        List<ListSellerUserDTO> sellerUsers;
        
        if (status != null && !status.isEmpty()) {
            // Lấy sellers theo status (ACTIVE, PENDING, INACTIVE)
            sellerUsers = userService.getSellersByStatus(status);
        } else {
            // Lấy tất cả sellers
            sellerUsers = userService.getSellerUsers();
        }
        
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Seller users retrieved successfully", sellerUsers));
    }
    
    /**
     * API lấy danh sách seller users với phân trang
     * GET /api/admin/users/sellers/page?page=0&size=10
     * @param page - Trang hiện tại (bắt đầu từ 0)
     * @param size - Số lượng items trên mỗi trang (mặc định 10)
     */
    @GetMapping("/admin/users/sellers/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<ListSellerUserDTO>>> getSellerUsersWithPaging(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ListSellerUserDTO> sellerUsers = userService.getSellerUsers(pageable);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Seller users retrieved successfully", sellerUsers));
    }

    @GetMapping("/admin/users/sellers/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<SellerStatsDTO>> getSellerStats() {
        SellerStatsDTO stats = userService.getSellerStats();
        return ResponseEntity.ok(
            new ResponseDTO<>(200, null, "Seller statistics retrieved successfully", stats)
        );
    }

    @GetMapping("/admin/users/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<ListCustomerUserDTO>>> getCustomerUsers() {
        List<ListCustomerUserDTO> customerUsers = userService.getCustomerUsers();
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Customer users retrieved successfully", customerUsers));
    }
    
    /**
     * API lấy danh sách customers với phân trang
     * GET /api/admin/users/customers/page?page=0&size=10
     * @param page - Trang hiện tại (bắt đầu từ 0)
     * @param size - Số lượng items trên mỗi trang (mặc định 10)
     */
    @GetMapping("/admin/users/customers/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<ListCustomerUserDTO>>> getCustomerUsersWithPaging(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ListCustomerUserDTO> customerUsers = userService.getCustomerUsers(pageable);
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
    
    // API xóa user http://127.0.0.1:8081/api/admin/users/{userId}
    @DeleteMapping("/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<String>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "User deleted successfully", "User has been removed from the system"));
    }

    /**
     * API cập nhật thông tin user (cho BUYER và ADMIN role)
     */
    @PatchMapping("/admin/users/{userId}/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<UserInfoDTO>> updateUserInfo(
            @PathVariable Long userId,
            @RequestBody AdminUpdateUserDTO dto) {
        UserInfoDTO updatedUser = userService.updateUserInfo(userId, dto);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "User updated successfully", updatedUser));
    }

    /**
     * API cập nhật thông tin seller (shop và user info)
     * PATCH /api/admin/seller/{userId}/update
     */
    @PatchMapping("/admin/seller/{userId}/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<UserInfoDTO>> updateSellerInfo(
            @PathVariable Long userId,
            @RequestBody AdminUpdateSellerDTO dto) {
        UserInfoDTO updatedSeller = userService.updateSellerInfo(userId, dto);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Seller updated successfully", updatedSeller));
    }

    /**
     * API tạo tài khoản admin mới
     * POST /api/admin/create-admin
     */
    @PostMapping("/admin/create-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<String>> createAdmin(@RequestBody AdminCreateAdminDTO dto) {
        String result = userService.createAdmin(dto);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Admin created successfully", result));
    }

        /**
     * API lấy thông tin profile của admin đang đăng nhập
     * GET /api/admin/myprofile
     * @return AdminMyProfileDTO chứa: id, username, fullName, email, phoneNumber, avatar, activated, createdAt
     */
    @GetMapping("/admin/myprofile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminMyProfileDTO>> getAdminMyProfile() {
        AdminMyProfileDTO profile = userService.getAdminMyProfile();
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Admin profile retrieved successfully", profile));
    }

    /**
     * API cập nhật thông tin profile của admin (không bao gồm avatar)
     * PATCH /api/admin/myprofile/update
     * @param dto - AdminUpdateMyProfileDTO chứa: username, fullName, email, phoneNumber
     * @return AdminMyProfileDTO sau khi cập nhật
     */
    @PatchMapping("/admin/myprofile/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminMyProfileDTO>> updateAdminMyProfile(
            @RequestBody AdminUpdateMyProfileDTO dto) {
        AdminMyProfileDTO updatedProfile = userService.updateAdminMyProfile(dto);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Admin profile updated successfully", updatedProfile));
    }

    /**
     * API đổi mật khẩu admin
     * POST /api/admin/myprofile/change-password
     * @param dto - AdminChangePasswordDTO chứa: oldPassword, newPassword, confirmPassword
     * @return Success message
     */
    @PostMapping("/admin/myprofile/change-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<String>> changeAdminPassword(
            @RequestBody AdminChangePasswordDTO dto) {
        userService.changeAdminPassword(dto);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Password changed successfully", "Your password has been updated"));
    }

    /**
     * API upload avatar cho admin
     * POST /api/admin/myprofile/avatar
     * @param avatarUrl - URL của avatar đã upload (từ service upload file riêng)
     * @return AdminMyProfileDTO sau khi cập nhật avatar
     */
    @PostMapping("/admin/myprofile/avatar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminMyProfileDTO>> updateAdminAvatar(
            @RequestParam("avatarUrl") String avatarUrl) {
        AdminMyProfileDTO updatedProfile = userService.updateAdminAvatar(avatarUrl);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Avatar updated successfully", updatedProfile));
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

            // Chỉ lấy 5 buyer cao nhất
            List<TopBuyerDTO> topBuyers = userService.getTopBuyersByShopWithLimit(username, 5);
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Lấy danh sách top 5 buyers của shop thành công", topBuyers)
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
    // @GetMapping("/seller/all-buyers")
    // @PreAuthorize("hasRole('SELLER')")
    // public ResponseEntity<ResponseDTO<List<TopBuyerDTO>>> getSellerBuyers() {
    //     try {
    //         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //         String username = authentication.getName();

    //         // Lấy tất cả buyers đã mua sản phẩm của shop (không giới hạn)
    //         List<TopBuyerDTO> buyers = userService.getBuyersByShop(username);
    //         return ResponseEntity.ok(
    //             new ResponseDTO<>(200, null, "Lấy danh sách tất cả buyers của shop thành công", buyers)
    //         );
    //     } catch (Exception e) {
    //         return ResponseEntity.badRequest().body(
    //             new ResponseDTO<>(400, e.getMessage(), "Lấy danh sách buyers thất bại", null)
    //         );
    //     }
    // }
}

