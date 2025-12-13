package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.domain.dto.admin.*;
import com.PBL6.Ecommerce.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin Dashboard Controller
 * Provides 4 main endpoints for admin dashboard statistics
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    /**
     * 1. Sales Overview - Get overall dashboard statistics
     * GET /api/admin/dashboard/stats
     * 
     * Returns:
     * - Total Revenue
     * - Total Orders
     * - Active Customers
     * - Conversion Rate
     * - Growth percentages for each metric
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminDashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    /**
     * 2. Sales by Category - Get sales breakdown by category
     * GET /api/admin/dashboard/sales-by-category
     * 
     * Returns list of categories with:
     * - Category Name
     * - Total Sales Amount
     * - Order Count
     */
    @GetMapping("/sales-by-category")
    public ResponseEntity<List<SalesByCategoryDTO>> getSalesByCategory() {
        return ResponseEntity.ok(dashboardService.getSalesByCategory());
    }

    /**
     * 3. Top Selling Products - Get top selling products
     * GET /api/admin/dashboard/top-products?limit=10
     * 
     * Returns list of top products with:
     * - Product ID, Name, Category, Image
     * - Quantity Sold
     * - Total Revenue
     * - Status (Active/Inactive)
     */
    @GetMapping("/top-products")
    public ResponseEntity<List<TopSellingProductDTO>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(dashboardService.getTopSellingProducts(limit));
    }

    /**
     * 4. Recent Orders - Get recent orders
     * GET /api/admin/dashboard/recent-orders?limit=10
     * 
     * Returns list of recent orders with:
     * - Order ID
     * - Customer Name & Email
     * - Order Date
     * - Total Amount
     * - Order Status
     */
    @GetMapping("/recent-orders")
    public ResponseEntity<List<RecentOrderDTO>> getRecentOrders(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(dashboardService.getRecentOrders(limit));
    }

    /**
     * 5. Revenue Chart - Get revenue data by month
     * GET /api/admin/dashboard/revenue-chart?months=12
     * 
     * Returns list of revenue data with:
     * - Period (Th7, Th8, Th9...)
     * - Revenue
     * - Order Count
     */
    @GetMapping("/revenue-chart")
    public ResponseEntity<List<RevenueChartDTO>> getRevenueChart(
            @RequestParam(defaultValue = "12") int months
    ) {
        return ResponseEntity.ok(dashboardService.getRevenueChart(months));
    }
}
