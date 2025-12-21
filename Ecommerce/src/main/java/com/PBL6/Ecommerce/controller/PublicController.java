package com.PBL6.Ecommerce.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.dto.ProductDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.VoucherDTO;
import com.PBL6.Ecommerce.domain.entity.product.Product;
import com.PBL6.Ecommerce.domain.entity.voucher.Vouchers;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.repository.VouchersRepository;
import com.PBL6.Ecommerce.service.ProductService;
import com.PBL6.Ecommerce.service.SoldCountUpdateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Public APIs", description = "Public endpoints - no authentication required. Get platform vouchers, best-selling products, and top-rated products")
@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final VouchersRepository vouchersRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final SoldCountUpdateService soldCountUpdateService;

    public PublicController(
            VouchersRepository vouchersRepository, 
            ProductRepository productRepository,
            ProductService productService,
            SoldCountUpdateService soldCountUpdateService) {
        this.vouchersRepository = vouchersRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.soldCountUpdateService = soldCountUpdateService;
    }

    @Operation(
        summary = "Get platform vouchers",
        description = "Retrieve all active vouchers from the platform (shop_id IS NULL). Platform vouchers can be used by all buyers across all shops. Returns vouchers that are currently active and within valid date range."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved platform vouchers",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Platform Vouchers Response",
                    value = "{\"status\":200,\"error\":null,\"message\":\"Lấy danh sách voucher sàn thành công\",\"data\":{\"vouchers\":[{\"id\":1,\"code\":\"PLATFORM10\",\"description\":\"Giảm 10% cho đơn hàng từ 100k\",\"shopId\":null,\"shopName\":\"Platform\",\"discountType\":\"PERCENTAGE\",\"discountValue\":10,\"minOrderValue\":100000,\"maxDiscountAmount\":50000,\"startDate\":\"2025-01-01T00:00:00\",\"endDate\":\"2025-12-31T23:59:59\",\"usageLimit\":1000,\"usedCount\":245,\"applicableType\":\"ALL\",\"status\":\"ACTIVE\"}],\"currentPage\":0,\"totalPages\":1,\"totalElements\":1,\"pageSize\":20}}"
                )
            )
        )
    })
    @GetMapping("/vouchers")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> getPlatformVouchers(
        @Parameter(description = "Page number (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            LocalDateTime now = LocalDateTime.now();
            
            // Find platform vouchers using repository query
            Page<Vouchers> vouchersPage = vouchersRepository.findActivePlatformVouchers(now, pageable);
            
            // Convert to DTOs
            Page<VoucherDTO> voucherDTOs = vouchersPage.map(this::convertToVoucherDTO);
            
            Map<String, Object> result = new HashMap<>();
            result.put("vouchers", voucherDTOs.getContent());
            result.put("currentPage", voucherDTOs.getNumber());
            result.put("totalPages", voucherDTOs.getTotalPages());
            result.put("totalElements", voucherDTOs.getTotalElements());
            result.put("pageSize", voucherDTOs.getSize());
            
            return ResponseDTO.success(result, "Lấy danh sách voucher sàn thành công");
        } catch (Exception e) {
            return ResponseDTO.error(500, "GET_PLATFORM_VOUCHERS_ERROR", "Lỗi khi lấy danh sách voucher: " + e.getMessage());
        }
    }

    @Operation(
        summary = "Get best-selling products",
        description = "Retrieve products ordered by sold_count DESC. sold_count is updated when orders are completed (DELIVERED status). Only active products are shown. Use this for homepage 'Trending Products' section."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved best-selling products",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Best-Selling Products Response",
                    value = "{\"status\":200,\"error\":null,\"message\":\"Lấy danh sách sản phẩm bán chạy thành công\",\"data\":{\"products\":[{\"id\":123,\"name\":\"iPhone 15 Pro Max\",\"basePrice\":29990000,\"soldCount\":1523,\"rating\":4.8,\"reviewCount\":342,\"mainImage\":\"https://...\",\"shopId\":45,\"shopName\":\"Apple Store Official\",\"isActive\":true}],\"currentPage\":0,\"totalPages\":5,\"totalElements\":100,\"pageSize\":20}}"
                )
            )
        )
    })
    @GetMapping("/products/best-selling")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> getBestSellingProducts(
        @Parameter(description = "Page number (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            // Find best-selling products using repository query
            Page<Product> productsPage = productRepository.findBestSellingProducts(pageable);
            
            // Convert to DTOs using ProductService
            Page<ProductDTO> productDTOs = productsPage.map(productService::convertToDTO);
            
            Map<String, Object> result = new HashMap<>();
            result.put("products", productDTOs.getContent());
            result.put("currentPage", productDTOs.getNumber());
            result.put("totalPages", productDTOs.getTotalPages());
            result.put("totalElements", productDTOs.getTotalElements());
            result.put("pageSize", productDTOs.getSize());
            
            return ResponseDTO.success(result, "Lấy danh sách sản phẩm bán chạy thành công");
        } catch (Exception e) {
            return ResponseDTO.error(500, "GET_BEST_SELLING_ERROR", "Lỗi khi lấy sản phẩm bán chạy: " + e.getMessage());
        }
    }

    @Operation(
        summary = "Get top-rated products",
        description = "Retrieve products ordered by average rating DESC. Only active products with rating > 0 are shown. Rating is calculated from product reviews. Use this for homepage 'Top Rated Products' section."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved top-rated products",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Top-Rated Products Response",
                    value = "{\"status\":200,\"error\":null,\"message\":\"Lấy danh sách sản phẩm được đánh giá cao thành công\",\"data\":{\"products\":[{\"id\":456,\"name\":\"Samsung Galaxy S24 Ultra\",\"basePrice\":27990000,\"soldCount\":892,\"rating\":4.95,\"reviewCount\":523,\"mainImage\":\"https://...\",\"shopId\":78,\"shopName\":\"Samsung Official Store\",\"isActive\":true}],\"currentPage\":0,\"totalPages\":3,\"totalElements\":50,\"pageSize\":20}}"
                )
            )
        )
    })
    @GetMapping("/products/top-rated")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> getTopRatedProducts(
        @Parameter(description = "Page number (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size,
        @Parameter(description = "Minimum rating filter (optional, 0-5)", example = "4.0")
        @RequestParam(required = false) Double minRating
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            Page<Product> productsPage;
            if (minRating != null && minRating > 0) {
                // Find with minimum rating filter
                productsPage = productRepository.findTopRatedProductsWithMinRating(
                    BigDecimal.valueOf(minRating), pageable);
            } else {
                // Find all top-rated products (rating > 0)
                productsPage = productRepository.findTopRatedProducts(pageable);
            }
            
            // Convert to DTOs using ProductService
            Page<ProductDTO> productDTOs = productsPage.map(productService::convertToDTO);
            
            Map<String, Object> result = new HashMap<>();
            result.put("products", productDTOs.getContent());
            result.put("currentPage", productDTOs.getNumber());
            result.put("totalPages", productDTOs.getTotalPages());
            result.put("totalElements", productDTOs.getTotalElements());
            result.put("pageSize", productDTOs.getSize());
            
            return ResponseDTO.success(result, "Lấy danh sách sản phẩm được đánh giá cao thành công");
        } catch (Exception e) {
            return ResponseDTO.error(500, "GET_TOP_RATED_ERROR", "Lỗi khi lấy sản phẩm đánh giá cao: " + e.getMessage());
        }
    }

    @Operation(
        summary = "Update sold_count for all products",
        description = "Manually trigger sold_count update for all products based on completed orders. This endpoint recalculates sold_count from order_items with COMPLETED status. Useful for data synchronization after bulk order processing."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully updated sold_count for all products",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Update Sold Count Response",
                    value = "{\"status\":200,\"error\":null,\"message\":\"Cập nhật sold_count cho tất cả sản phẩm thành công\",\"data\":{\"message\":\"All products' sold_count updated successfully\"}}"
                )
            )
        )
    })
    @PostMapping("/products/update-sold-count")
    public ResponseEntity<ResponseDTO<Map<String, String>>> updateSoldCount() {
        try {
            soldCountUpdateService.updateAllSoldCounts();
            
            Map<String, String> result = new HashMap<>();
            result.put("message", "All products' sold_count updated successfully");
            
            return ResponseDTO.success(result, "Cập nhật sold_count cho tất cả sản phẩm thành công");
        } catch (Exception e) {
            return ResponseDTO.error(500, "UPDATE_SOLD_COUNT_ERROR", "Lỗi khi cập nhật sold_count: " + e.getMessage());
        }
    }

    @Operation(
        summary = "Update rating and review_count for all products",
        description = "Manually trigger rating update for all products based on actual reviews. Recalculates average rating and review count from product_reviews table."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully updated ratings for all products",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Update Rating Response",
                    value = "{\"status\":200,\"error\":null,\"message\":\"Cập nhật rating cho tất cả sản phẩm thành công\",\"data\":{\"message\":\"All products' ratings updated successfully\"}}"
                )
            )
        )
    })
    @PostMapping("/products/update-rating")
    public ResponseEntity<ResponseDTO<Map<String, String>>> updateRating() {
        try {
            productService.updateAllProductRatings();
            
            Map<String, String> result = new HashMap<>();
            result.put("message", "All products' ratings updated successfully");
            
            return ResponseDTO.success(result, "Cập nhật rating cho tất cả sản phẩm thành công");
        } catch (Exception e) {
            return ResponseDTO.error(500, "UPDATE_RATING_ERROR", "Lỗi khi cập nhật rating: " + e.getMessage());
        }
    }

    /**
     * Helper method to convert Vouchers entity to VoucherDTO
     */
    private VoucherDTO convertToVoucherDTO(Vouchers voucher) {
        VoucherDTO dto = new VoucherDTO();
        dto.setId(voucher.getId());
        dto.setCode(voucher.getCode());
        dto.setDescription(voucher.getDescription());
        dto.setShopId(voucher.getShop() != null ? voucher.getShop().getId() : null);
        dto.setShopName(voucher.getShop() != null ? voucher.getShop().getName() : "Platform");
        dto.setDiscountType(voucher.getDiscountType());
        dto.setDiscountValue(voucher.getDiscountValue());
        dto.setMinOrderValue(voucher.getMinOrderValue());
        dto.setMaxDiscountAmount(voucher.getMaxDiscountAmount());
        dto.setStartDate(voucher.getStartDate());
        dto.setEndDate(voucher.getEndDate());
        dto.setUsageLimit(voucher.getUsageLimit());
        dto.setUsedCount(voucher.getUsedCount());
        dto.setApplicableType(voucher.getApplicableType());
        dto.setTopBuyersCount(voucher.getTopBuyersCount());
        dto.setStatus(voucher.getStatus());
        dto.setCreatedAt(voucher.getCreatedAt());
        return dto;
    }
}

