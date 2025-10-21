package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.domain.dto.AdminUserDetailDTO;
import com.PBL6.Ecommerce.domain.dto.CheckContactDTO;
import com.PBL6.Ecommerce.domain.dto.VerifyOtpDTO;
import com.PBL6.Ecommerce.domain.dto.RegisterDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateUserRoleDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateUserStatusDTO;
import com.PBL6.Ecommerce.domain.dto.UserInfoDTO;
import com.PBL6.Ecommerce.domain.dto.UserListDTO;
import com.PBL6.Ecommerce.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
        try {
            String result = userService.checkContact(dto);
            ResponseDTO<String> response = new ResponseDTO<>(200, null, "Thành công", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<String> response = new ResponseDTO<>(400, e.getMessage(), "Thất bại", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/register/verify-otp")
    public ResponseEntity<ResponseDTO<String>> verifyOtp(@RequestBody VerifyOtpDTO dto) {
        try {
            String result = userService.verifyOtp(dto);
            ResponseDTO<String> response = new ResponseDTO<>(200, null, "Thành công", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<String> response = new ResponseDTO<>(400, e.getMessage(), "Thất bại", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/register/register")
    public ResponseEntity<ResponseDTO<String>> register(@RequestBody RegisterDTO dto) {
        try {
            String result = userService.register(dto);
            ResponseDTO<String> response = new ResponseDTO<>(200, null, "Thành công", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<String> response = new ResponseDTO<>(400, e.getMessage(), "Thất bại", null);
            return ResponseEntity.badRequest().body(response);
        }
    }
    @GetMapping("/user/me")
    public ResponseEntity<ResponseDTO<UserInfoDTO>> getCurrentUser() {
        try {
            UserInfoDTO userInfo = userService.getCurrentUser();
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "Lấy thông tin thành công", userInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Thất bại", null)
            );
        }
    }



    // Admin APIs - Chỉ admin mới có thể truy cập
    @GetMapping("/admin/users/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<UserInfoDTO>>> getAdminUsers() {
        try {
            List<UserInfoDTO> adminUsers = userService.getUsersByRole("ADMIN");
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "Lấy danh sách admin thành công", adminUsers));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Thất bại", null)
            );
        }
    }

    @GetMapping("/admin/users/sellers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<UserInfoDTO>>> getSellerUsers() {
        try {
            List<UserInfoDTO> sellerUsers = userService.getUsersByRole("SELLER");
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "Lấy danh sách seller thành công", sellerUsers));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Thất bại", null)
            );
        }
    }

    @GetMapping("/admin/users/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<UserInfoDTO>>> getCustomerUsers() {
        try {
            List<UserInfoDTO> customerUsers = userService.getUsersByRole("BUYER");
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "Lấy danh sách customer thành công", customerUsers));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Thất bại", null)
            );
        }
    }
    

    // API lấy chi tiết 1 user theo ID
    @GetMapping("/admin/users/detail/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminUserDetailDTO>> getUserDetail(@PathVariable Long userId) {
        try {
            AdminUserDetailDTO userDetail = userService.getUserDetailById(userId);
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "Lấy chi tiết user thành công", userDetail));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Thất bại", null)
            );
        }
    }

    // API thay đổi role của user
    @PatchMapping("/admin/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<UserInfoDTO>> updateUserRole(
            @PathVariable Long userId,
            @RequestBody UpdateUserRoleDTO dto) {
        try {
            UserInfoDTO updatedUser = userService.updateUserRole(userId, dto.getRole());
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "Cập nhật role thành công", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Cập nhật role thất bại", null)
            );
        }
    }

    // API thay đổi trạng thái user (active/inactive)
    @PatchMapping("/admin/users/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<UserInfoDTO>> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UpdateUserStatusDTO dto) {
        try {
            UserInfoDTO updatedUser = userService.updateUserStatus(userId, dto.isActivated());
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "Cập nhật trạng thái thành công", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Cập nhật trạng thái thất bại", null)
            );
        }
    }
    
    // API xóa user http://127.0.0.1:8081/api/admin/users/{userId}
    @DeleteMapping("/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<String>> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "Xóa user thành công", "User đã được xóa khỏi hệ thống"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Xóa user thất bại", null)
            );
        }
    }
}

