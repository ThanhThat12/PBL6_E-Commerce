package com.PBL6.Ecommerce.controller.seller;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.seller.SellerDashboardStatsDTO;
import com.PBL6.Ecommerce.service.SellerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Seller Dashboard Controller
 * Endpoints for seller dashboard statistics and analytics
 */
@RestController
@RequestMapping("/api/seller/dashboard")
@RequiredArgsConstructor
public class SellerDashboardController {

    private final SellerDashboardService dashboardService;

    /**
     * Get seller dashboard statistics
     * GET /api/seller/dashboard/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<SellerDashboardStatsDTO>> getDashboardStats(Authentication authentication) {
        try {
            SellerDashboardStatsDTO stats = dashboardService.getDashboardStats(authentication);
            return ResponseDTO.success(stats, "Lấy thống kê dashboard thành công");
        } catch (Exception e) {
            return ResponseDTO.error(400, "GET_STATS_ERROR", e.getMessage());
        }
    }
}
