package com.PBL6.Ecommerce.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.AdminApprovalDTO;
import com.PBL6.Ecommerce.domain.dto.AdminRejectionDTO;
import com.PBL6.Ecommerce.domain.dto.PendingApplicationDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.SellerRegistrationResponseDTO;
import com.PBL6.Ecommerce.service.SellerRegistrationService;
import com.PBL6.Ecommerce.service.UserService;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Admin endpoints for managing seller registration applications
 */
@RestController
@RequestMapping("/api/admin/seller-registrations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSellerRegistrationController {

    private final SellerRegistrationService sellerRegistrationService;
    private final UserService userService;

    public AdminSellerRegistrationController(SellerRegistrationService sellerRegistrationService,
                                              UserService userService) {
        this.sellerRegistrationService = sellerRegistrationService;
        this.userService = userService;
    }

    /**
     * Get list of pending seller registration applications
     * GET /api/admin/seller-registrations/pending
     * 
     * @param page - Page number (0-indexed)
     * @param size - Page size (default 10)
     * @return Page of PendingApplicationDTO
     */
    @GetMapping("/pending")
    public ResponseEntity<ResponseDTO<Page<PendingApplicationDTO>>> getPendingApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PendingApplicationDTO> pendingApps = sellerRegistrationService.getPendingApplications(pageable);

            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Lấy danh sách đơn đăng ký thành công", pendingApps)
            );

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ResponseDTO<>(500, e.getMessage(), "Lỗi hệ thống", null)
            );
        }
    }

    /**
     * Search pending applications by keyword
     * GET /api/admin/seller-registrations/search
     * 
     * @param keyword - Search keyword (shop name, email, phone)
     * @param page - Page number
     * @param size - Page size
     */
    @GetMapping("/search")
    public ResponseEntity<ResponseDTO<Page<PendingApplicationDTO>>> searchPendingApplications(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PendingApplicationDTO> results = sellerRegistrationService.searchPendingApplications(keyword, pageable);

            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Tìm kiếm thành công", results)
            );

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ResponseDTO<>(500, e.getMessage(), "Lỗi hệ thống", null)
            );
        }
    }

    /**
     * Get count of pending applications (for dashboard badge)
     * GET /api/admin/seller-registrations/pending/count
     */
    @GetMapping("/pending/count")
    public ResponseEntity<ResponseDTO<Map<String, Long>>> getPendingCount() {
        try {
            long count = sellerRegistrationService.countPendingApplications();
            
            Map<String, Long> result = new HashMap<>();
            result.put("count", count);

            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Lấy số lượng thành công", result)
            );

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ResponseDTO<>(500, e.getMessage(), "Lỗi hệ thống", null)
            );
        }
    }

    /**
     * Get single application detail
     * GET /api/admin/seller-registrations/{shopId}
     */
    @GetMapping("/{shopId}")
    public ResponseEntity<ResponseDTO<PendingApplicationDTO>> getApplicationDetail(@PathVariable Long shopId) {
        try {
            PendingApplicationDTO application = sellerRegistrationService.getApplicationDetail(shopId);

            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Lấy chi tiết đơn đăng ký thành công", application)
            );

        } catch (RuntimeException e) {
            String errorMessage = e.getMessage();
            int statusCode = errorMessage.contains("Không tìm thấy") ? 404 : 400;

            return ResponseEntity.status(statusCode).body(
                new ResponseDTO<>(statusCode, errorMessage, "Lấy chi tiết thất bại", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ResponseDTO<>(500, e.getMessage(), "Lỗi hệ thống", null)
            );
        }
    }

    /**
     * Approve seller registration
     * POST /api/admin/seller-registrations/approve
     */
    @PostMapping("/approve")
    public ResponseEntity<ResponseDTO<SellerRegistrationResponseDTO>> approveRegistration(
            @Valid @RequestBody AdminApprovalDTO approvalDTO,
            Authentication authentication) {
        try {
            User admin = userService.resolveCurrentUser(authentication);
            SellerRegistrationResponseDTO response = sellerRegistrationService.approveRegistration(admin, approvalDTO);

            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, response.getMessage(), response)
            );

        } catch (RuntimeException e) {
            String errorMessage = e.getMessage();
            int statusCode;

            if (errorMessage.contains("Chỉ Admin")) {
                statusCode = 403;
            } else if (errorMessage.contains("Không tìm thấy")) {
                statusCode = 404;
            } else if (errorMessage.contains("không ở trạng thái chờ duyệt")) {
                statusCode = 409;
            } else {
                statusCode = 400;
            }

            return ResponseEntity.status(statusCode).body(
                new ResponseDTO<>(statusCode, errorMessage, "Phê duyệt thất bại", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ResponseDTO<>(500, e.getMessage(), "Lỗi hệ thống", null)
            );
        }
    }

    /**
     * Reject seller registration
     * POST /api/admin/seller-registrations/reject
     */
    @PostMapping("/reject")
    public ResponseEntity<ResponseDTO<SellerRegistrationResponseDTO>> rejectRegistration(
            @Valid @RequestBody AdminRejectionDTO rejectionDTO,
            Authentication authentication) {
        try {
            User admin = userService.resolveCurrentUser(authentication);
            SellerRegistrationResponseDTO response = sellerRegistrationService.rejectRegistration(admin, rejectionDTO);

            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, response.getMessage(), response)
            );

        } catch (RuntimeException e) {
            String errorMessage = e.getMessage();
            int statusCode;

            if (errorMessage.contains("Chỉ Admin")) {
                statusCode = 403;
            } else if (errorMessage.contains("Không tìm thấy")) {
                statusCode = 404;
            } else if (errorMessage.contains("không ở trạng thái chờ duyệt")) {
                statusCode = 409;
            } else {
                statusCode = 400;
            }

            return ResponseEntity.status(statusCode).body(
                new ResponseDTO<>(statusCode, errorMessage, "Từ chối thất bại", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ResponseDTO<>(500, e.getMessage(), "Lỗi hệ thống", null)
            );
        }
    }
}
