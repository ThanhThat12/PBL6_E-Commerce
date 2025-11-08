package com.PBL6.Ecommerce.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.PBL6.Ecommerce.domain.dto.ProductCreateDTO;
import com.PBL6.Ecommerce.domain.dto.ProductDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.service.CloudinaryService;
import com.PBL6.Ecommerce.service.SellerProductService;

import jakarta.validation.Valid;

/**
 * Seller Product Controller - Phase 2
 * Frontend compatible endpoints for seller product management
 * All endpoints require SELLER role
 */
@RestController
@RequestMapping("/api/seller/products")
public class SellerProductController {
    
    private final SellerProductService sellerProductService;
    private final CloudinaryService cloudinaryService;
    
    public SellerProductController(SellerProductService sellerProductService,
                                  CloudinaryService cloudinaryService) {
        this.sellerProductService = sellerProductService;
        this.cloudinaryService = cloudinaryService;
    }
    
    /**
     * Upload product image
     * POST /api/seller/products/upload-image
     */
    @PostMapping(value = "/upload-image", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<String>> uploadProductImage(
            @RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new ResponseDTO<>(400, "File không được để trống", null, null)
                );
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(
                    new ResponseDTO<>(400, "File phải là hình ảnh", null, null)
                );
            }
            
            String imageUrl = cloudinaryService.uploadProductImage(file, 0L);
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Upload ảnh thành công", imageUrl)
            );
        } catch (Exception e) {
            e.printStackTrace(); // Debug log
            return ResponseEntity.status(500).body(
                new ResponseDTO<>(500, e.getMessage(), "Upload ảnh thất bại", null)
            );
        }
    }
    
    /**
     * Create new product (seller)
     * POST /api/seller/products
     */
    @PostMapping(value = "", consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<ProductDTO>> createSellerProduct(
            @Valid @RequestBody ProductCreateDTO request,
            Authentication authentication) {
        try {
            ProductDTO product = sellerProductService.createSellerProduct(request, authentication);
            return ResponseEntity.ok(
                new ResponseDTO<>(201, null, "Tạo sản phẩm thành công (chờ duyệt)", product)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Tạo sản phẩm thất bại", null)
            );
        }
    }
    
    /**
     * Get seller products with filters (frontend compatible)
     * GET /api/seller/products?page=0&size=10&keyword=&categoryId=&status=
     * 
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @param sortBy Sort field (default: "id")
     * @param sortDir Sort direction (default: "desc")
     * @param keyword Search keyword for product name
     * @param categoryId Filter by category
     * @param status Filter by active status
     * @return Paginated list of products
     */
    @GetMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<Page<ProductDTO>>> getSellerProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean status,
            Authentication authentication) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Use existing service with filters
            Page<ProductDTO> products = sellerProductService.getSellerProductsWithFilters(
                authentication, keyword, categoryId, status, pageable);
            
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Lấy sản phẩm của seller thành công", products)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Lấy sản phẩm thất bại", null)
            );
        }
    }
    
    /**
     * Get seller product by ID
     * GET /api/seller/products/{id}
     * Verify ownership before returning
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<ProductDTO>> getSellerProduct(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            ProductDTO product = sellerProductService.getSellerProductById(id, authentication);
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Lấy thông tin sản phẩm thành công", product)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Lấy thông tin sản phẩm thất bại", null)
            );
        }
    }
    
    /**
     * Update seller product
     * PUT /api/seller/products/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<ProductDTO>> updateSellerProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductCreateDTO request,
            Authentication authentication) {
        try {
            ProductDTO product = sellerProductService.updateSellerProduct(id, request, authentication);
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Cập nhật sản phẩm thành công", product)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Cập nhật sản phẩm thất bại", null)
            );
        }
    }
    
    /**
     * Toggle product active status
     * PATCH /api/seller/products/{id}/status?status=true
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<ProductDTO>> toggleSellerProductStatus(
            @PathVariable Long id,
            @RequestParam Boolean status,
            Authentication authentication) {
        try {
            ProductDTO product = sellerProductService.toggleSellerProductStatus(id, status, authentication);
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Cập nhật trạng thái sản phẩm thành công", product)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Cập nhật trạng thái thất bại", null)
            );
        }
    }

    /**
     * Delete seller product
     * DELETE /api/seller/products/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<String>> deleteSellerProduct(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            sellerProductService.deleteSellerProduct(id, authentication);
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Xóa sản phẩm thành công", null)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Xóa sản phẩm thất bại", null)
            );
        }
    }
}
