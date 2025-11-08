package com.PBL6.Ecommerce.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.dto.OrderDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.dto.seller.DashboardStatsDTO;
import com.PBL6.Ecommerce.dto.seller.RevenueDataDTO;
import com.PBL6.Ecommerce.dto.seller.TopProductDTO;
import com.PBL6.Ecommerce.service.SellerDashboardService;

/**
 * Seller Dashboard Controller
 * Provides statistics and analytics for seller dashboard
 * All endpoints require SELLER role
 */
@RestController
@RequestMapping("/api/seller/dashboard")
public class SellerDashboardController {
    
    private final SellerDashboardService dashboardService;
    
    public SellerDashboardController(SellerDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    
    /**
     * Get dashboard statistics overview
     * GET /api/seller/dashboard/stats
     * 
     * @return DashboardStatsDTO with totalRevenue, totalOrders, totalProducts, totalCustomers
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<DashboardStatsDTO>> getDashboardStats(
            Authentication authentication) {
        try {
            String username = authentication.getName();
            DashboardStatsDTO stats = dashboardService.getDashboardStats(username);
            
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Lấy thống kê dashboard thành công", stats)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Lấy thống kê dashboard thất bại", null)
            );
        }
    }
    
    /**
     * Get revenue statistics by time range
     * GET /api/seller/dashboard/revenue?timeRange=month
     * 
     * @param timeRange "week", "month", "year" (default: "month")
     * @return List of RevenueDataDTO for charts
     */
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<List<RevenueDataDTO>>> getRevenueStats(
            @RequestParam(defaultValue = "month") String timeRange,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            List<RevenueDataDTO> revenueData = dashboardService.getRevenueStats(username, timeRange);
            
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Lấy thống kê doanh thu thành công", revenueData)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Lấy thống kê doanh thu thất bại", null)
            );
        }
    }
    
    /**
     * Get recent orders for dashboard
     * GET /api/seller/dashboard/recent-orders?limit=5
     * 
     * @param limit Number of orders to return (default: 5)
     * @return List of recent OrderDTO
     */
    @GetMapping("/recent-orders")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<List<OrderDTO>>> getRecentOrders(
            @RequestParam(defaultValue = "5") int limit,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            List<OrderDTO> orders = dashboardService.getRecentOrders(username, limit);
            
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Lấy đơn hàng gần đây thành công", orders)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Lấy đơn hàng gần đây thất bại", null)
            );
        }
    }
    
    /**
     * Get top selling products
     * GET /api/seller/dashboard/top-products?limit=5
     * 
     * @param limit Number of products to return (default: 5)
     * @return List of TopProductDTO
     */
    @GetMapping("/top-products")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<List<TopProductDTO>>> getTopProducts(
            @RequestParam(defaultValue = "5") int limit,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            List<TopProductDTO> topProducts = dashboardService.getTopProducts(username, limit);
            
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Lấy sản phẩm bán chạy thành công", topProducts)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Lấy sản phẩm bán chạy thất bại", null)
            );
        }
    }
}
