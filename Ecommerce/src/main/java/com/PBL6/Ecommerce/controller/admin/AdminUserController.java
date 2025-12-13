package com.PBL6.Ecommerce.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.admin.ListCustomerUserDTO;
import com.PBL6.Ecommerce.domain.dto.admin.ListSellerUserDTO;
import com.PBL6.Ecommerce.service.AdminUserService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin Users", description = "Admin user management APIs")
@RestController
@RequestMapping("/api/admin")
public class AdminUserController {
    
    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * API lấy danh sách customers theo status với phân trang
     * GET /api/admin/users/customers/status?status=ACTIVE&page=0&size=10
     * @param status - Trạng thái: ACTIVE (activated=true), INACTIVE (activated=false) hoặc ALL (mặc định lấy tất cả)
     * @param page - Trang hiện tại (bắt đầu từ 0)
     * @param size - Số lượng items trên mỗi trang (mặc định 10)
     * @return Page<ListCustomerUserDTO> - Danh sách customers với phân trang
     */
    @GetMapping("/users/customers/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<ListCustomerUserDTO>>> getCustomersByStatusWithPaging(
            @RequestParam(value = "status", required = false, defaultValue = "ALL") String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        // Validate status parameter
        if (status != null && !status.isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            if (!"ACTIVE".equalsIgnoreCase(status) && !"INACTIVE".equalsIgnoreCase(status)) {
                return ResponseEntity.badRequest().body(
                    new ResponseDTO<>(400, "INVALID_STATUS", 
                        "Invalid status. Valid values: ACTIVE, INACTIVE, ALL", null)
                );
            }
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ListCustomerUserDTO> customers = adminUserService.getCustomersByStatusWithPaging(status, pageable);
        
        String message = status == null || "ALL".equalsIgnoreCase(status) 
            ? "All customers retrieved successfully" 
            : String.format("Customers with status '%s' retrieved successfully", status.toUpperCase());
            
        return ResponseEntity.ok(new ResponseDTO<>(200, null, message, customers));
    }

    /**
     * API lấy danh sách sellers từ bảng shop với phân trang
     * GET /api/admin/users/seller?page=0&size=10
     * @param page - Trang hiện tại (bắt đầu từ 0)
     * @param size - Số lượng items trên mỗi trang (mặc định 10)
     * @return Page<ListSellerUserDTO> - Danh sách sellers với phân trang
     */
    @GetMapping("/users/seller")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<ListSellerUserDTO>>> getSellersWithPaging(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ListSellerUserDTO> sellers = adminUserService.getSellersWithPaging(pageable);
        
        return ResponseEntity.ok(new ResponseDTO<>(200, null, 
            "Sellers retrieved successfully", sellers));
    }
}