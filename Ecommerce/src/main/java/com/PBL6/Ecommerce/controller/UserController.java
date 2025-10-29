package com.PBL6.Ecommerce.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.dto.AdminUserDetailDTO;
import com.PBL6.Ecommerce.domain.dto.CheckContactDTO;
import com.PBL6.Ecommerce.domain.dto.RegisterDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateUserRoleDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateUserStatusDTO;
import com.PBL6.Ecommerce.domain.dto.UserInfoDTO;
import com.PBL6.Ecommerce.domain.dto.VerifyOtpDTO;
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
    public ResponseEntity<ResponseDTO<List<UserInfoDTO>>> getCustomerUsers() {
        List<UserInfoDTO> customerUsers = userService.getUsersByRole("BUYER");
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Customer users retrieved successfully", customerUsers));
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
}

