package com.PBL6.Ecommerce.controller.product;

import com.PBL6.Ecommerce.domain.dto.ProductDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public Product Controller - APIs công khai cho homepage
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Public Products", description = "Public APIs for product discovery")
public class PublicProductController {
    
    @Autowired
    private ProductService productService;

    /**
     * Lấy sản phẩm bán chạy (best-selling) cho homepage
     * GET /api/products/best-selling?page=0&size=8
     * Sort by soldCount DESC
     */
    @Operation(
        summary = "Get best-selling products for homepage",
        description = "Get products sorted by sold count (highest first)"
    )
    @GetMapping("/best-selling")
    public ResponseEntity<ResponseDTO<Page<ProductDTO>>> getBestSellingProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductDTO> products = productService.getBestSellingProducts(pageable);
            return ResponseDTO.success(products, "Lấy sản phẩm bán chạy thành công");
        } catch (Exception e) {
            return ResponseDTO.error(400, "GET_BEST_SELLING_ERROR", e.getMessage());
        }
    }
}
