package com.PBL6.Ecommerce.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.dto.seller.CustomerStatsDTO;
import com.PBL6.Ecommerce.dto.seller.OrderStatusDistributionDTO;
import com.PBL6.Ecommerce.dto.seller.SalesDataDTO;
import com.PBL6.Ecommerce.dto.seller.TopProductDTO;
import com.PBL6.Ecommerce.service.SellerStatisticalService;

@RestController
@RequestMapping("/api/seller/statistical")
@PreAuthorize("hasRole('SELLER')")
public class SellerStatisticalController {

    @Autowired
    private SellerStatisticalService statisticalService;

    /**
     * GET /api/seller/statistical/revenue?start=2024-01-01&end=2024-12-31
     * Lấy dữ liệu doanh thu theo thời gian
     */
    @GetMapping("/revenue")
    public ResponseEntity<List<SalesDataDTO>> getRevenueData(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        String username = authentication.getName();
        
        // Default: last 30 days
        if (start == null) {
            start = LocalDate.now().minusDays(30);
        }
        if (end == null) {
            end = LocalDate.now();
        }
        
        List<SalesDataDTO> revenueData = statisticalService.getRevenueData(username, start, end);
        return ResponseEntity.ok(revenueData);
    }

    /**
     * GET /api/seller/statistical/sales?start=2024-01-01&end=2024-12-31
     * Lấy dữ liệu bán hàng (số lượng đơn + doanh thu)
     */
    @GetMapping("/sales")
    public ResponseEntity<List<SalesDataDTO>> getSalesData(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        String username = authentication.getName();
        
        // Default: last 30 days
        if (start == null) {
            start = LocalDate.now().minusDays(30);
        }
        if (end == null) {
            end = LocalDate.now();
        }
        
        List<SalesDataDTO> salesData = statisticalService.getSalesData(username, start, end);
        return ResponseEntity.ok(salesData);
    }

    /**
     * GET /api/seller/statistical/top-products?start=2024-01-01&end=2024-12-31&limit=10
     * Lấy danh sách sản phẩm bán chạy
     */
    @GetMapping("/top-products")
    public ResponseEntity<List<TopProductDTO>> getTopProducts(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "10") int limit
    ) {
        String username = authentication.getName();
        
        // Default: last 30 days
        if (start == null) {
            start = LocalDate.now().minusDays(30);
        }
        if (end == null) {
            end = LocalDate.now();
        }
        
        List<TopProductDTO> topProducts = statisticalService.getTopProducts(username, start, end, limit);
        return ResponseEntity.ok(topProducts);
    }

    /**
     * GET /api/seller/statistical/customers
     * Lấy thống kê về khách hàng
     */
    @GetMapping("/customers")
    public ResponseEntity<CustomerStatsDTO> getCustomerAnalytics(
            Authentication authentication
    ) {
        String username = authentication.getName();
        CustomerStatsDTO stats = statisticalService.getCustomerAnalytics(username);
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/seller/statistical/order-status
     * Lấy phân bố trạng thái đơn hàng
     */
    @GetMapping("/order-status")
    public ResponseEntity<List<OrderStatusDistributionDTO>> getOrderStatusDistribution(
            Authentication authentication
    ) {
        String username = authentication.getName();
        List<OrderStatusDistributionDTO> distribution = statisticalService.getOrderStatusDistribution(username);
        return ResponseEntity.ok(distribution);
    }

    /**
     * GET /api/seller/statistical/export?start=2024-01-01&end=2024-12-31
     * Export dữ liệu thống kê (Optional - for future)
     */
    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportStatistics(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        String username = authentication.getName();
        
        // Default: last 30 days
        if (start == null) {
            start = LocalDate.now().minusDays(30);
        }
        if (end == null) {
            end = LocalDate.now();
        }
        
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("revenue", statisticalService.getRevenueData(username, start, end));
        exportData.put("sales", statisticalService.getSalesData(username, start, end));
        exportData.put("topProducts", statisticalService.getTopProducts(username, start, end, 20));
        exportData.put("customers", statisticalService.getCustomerAnalytics(username));
        exportData.put("orderStatus", statisticalService.getOrderStatusDistribution(username));
        exportData.put("exportDate", LocalDate.now());
        exportData.put("period", Map.of("start", start, "end", end));
        
        return ResponseEntity.ok(exportData);
    }
}
