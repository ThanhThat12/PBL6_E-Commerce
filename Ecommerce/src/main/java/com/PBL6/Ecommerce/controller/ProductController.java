package com.PBL6.Ecommerce.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.PBL6.Ecommerce.dto.seller.VariantImageDTO;
import com.PBL6.Ecommerce.service.ProductService;
import com.PBL6.Ecommerce.service.VariantImageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;

    @Autowired
    private VariantImageService variantImageService;



    // Lấy tất cả sản phẩm công khai (không phân trang)
    @GetMapping("/all")
    public ResponseEntity<ResponseDTO<List<ProductDTO>>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseDTO.success(products, "Lấy danh sách sản phẩm thành công");
    }
    
    // Tạo sản phẩm mới - Chỉ admin hoặc seller
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<ProductDTO>> createProduct(
            @Valid @RequestBody ProductCreateDTO request,
            Authentication authentication) {
        ProductDTO product = productService.createProduct(request, authentication);
        return ResponseDTO.created(product, "Tạo sản phẩm thành công");
    }

    // 🆕 Admin duyệt/từ chối sản phẩm
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<ResponseDTO<ProductDTO>> approveProduct(
            @PathVariable Long id,
            @RequestParam Boolean approved) {
        try {
            ProductDTO product = productService.approveProduct(id, approved);
            String message = approved ? "Duyệt sản phẩm thành công" : "Từ chối sản phẩm thành công";
            ResponseDTO<ProductDTO> response = new ResponseDTO<>(200, null, message, product);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<ProductDTO> response = new ResponseDTO<>(400, "BAD_REQUEST", "Lỗi khi xử lý duyệt sản phẩm: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 🆕 Lấy danh sách sản phẩm chờ duyệt (chỉ admin)
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<ProductDTO>>> getPendingProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ProductDTO> products = productService.getPendingProducts(pageable);
            ResponseDTO<Page<ProductDTO>> response = new ResponseDTO<>(200, null, "Lấy danh sách sản phẩm chờ duyệt thành công", products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<Page<ProductDTO>> response = new ResponseDTO<>(400, "BAD_REQUEST", "Lỗi khi lấy sản phẩm chờ duyệt: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }

        // 🆕 Đếm số sản phẩm chờ duyệt
    @GetMapping("/pending/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Long>> countPendingProducts() {
        try {
            long count = productService.countPendingProducts();
            ResponseDTO<Long> response = new ResponseDTO<>(200, null, "Đếm sản phẩm chờ duyệt thành công", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<Long> response = new ResponseDTO<>(400, "BAD_REQUEST", "Lỗi khi đếm sản phẩm chờ duyệt: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
   
    
    // Lấy tất cả sản phẩm - Admin xem tất cả, Seller chỉ xem của mình
    @GetMapping("/manage")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<Page<ProductDTO>>> getAllProductsForManagement(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductDTO> products = productService.getAllProductsForManagement(pageable, authentication);
        return ResponseDTO.success(products, "Lấy danh sách sản phẩm quản lý thành công");
    }
    
    // Lấy tất cả sản phẩm công khai (có phân trang)
    @GetMapping
    public ResponseEntity<ResponseDTO<Page<ProductDTO>>> getAllActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        // Validation
        if (page < 0) {
            return ResponseDTO.badRequest("Page number không được nhỏ hơn 0");
        }
        if (size < 1 || size > 100) {
            return ResponseDTO.badRequest("Page size phải từ 1 đến 100");
        }
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductDTO> products = productService.getAllActiveProducts(pageable);
        return ResponseDTO.success(products, "Lấy danh sách sản phẩm hoạt động thành công");
    }
    
    // Lấy sản phẩm theo ID - Công khai
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<ProductDTO>> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseDTO.success(product, "Lấy thông tin sản phẩm thành công");
    }
    
    // Tìm kiếm sản phẩm - Công khai
    @GetMapping("/search")
    public ResponseEntity<ResponseDTO<Page<ProductDTO>>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductDTO> products = productService.searchActiveProducts(
            name, categoryId, shopId, minPrice, maxPrice, pageable);
        return ResponseDTO.success(products, "Tìm kiếm sản phẩm thành công");
    }
    
    // Lấy sản phẩm theo category - Công khai (có phân trang)
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ResponseDTO<Page<ProductDTO>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.getActiveProductsByCategory(categoryId, pageable);
        return ResponseDTO.success(products, "Lấy sản phẩm theo danh mục thành công");
    }
    
    // Lấy sản phẩm theo category - Công khai (không phân trang)
    @GetMapping("/category/{categoryId}/all")
    public ResponseEntity<ResponseDTO<List<ProductDTO>>> getAllProductsByCategory(@PathVariable Long categoryId) {
        List<ProductDTO> products = productService.getProductsByCategory(categoryId);
        return ResponseDTO.success(products, "Lấy tất cả sản phẩm theo danh mục thành công");
    }
    

    // Cập nhật sản phẩm - Admin hoặc seller sở hữu sản phẩm
    // @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @productService.isProductOwner(#id, authentication.name))")
    // public ResponseEntity<ResponseDTO<ProductDTO>> updateProduct(
    //         @PathVariable Long id, 
    //         @Valid @RequestBody ProductCreateDTO request,
    //         Authentication authentication) {
    //     ProductDTO product = productService.updateProduct(id, request, authentication);
    //     return ResponseDTO.success(product, "Cập nhật sản phẩm thành công");
    // }

    // Xóa sản phẩm - Admin hoặc seller sở hữu sản phẩm
     @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<String>> deleteProduct(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            productService.deleteProduct(id, authentication);
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "Xóa sản phẩm thành công", "Product deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, "BAD_REQUEST", "Lỗi khi xóa sản phẩm: " + e.getMessage(), null)
            );
        }

    }
    
    // ==================== VARIANT IMAGE MANAGEMENT ====================

    /**
     * Upload an image for a specific product variant
     * POST /api/seller/products/{productId}/variants/{variantId}/images
     */
    @PostMapping("/{productId}/variants/{variantId}/images")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<VariantImageDTO>> uploadVariantImage(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @RequestParam("image") MultipartFile file,
            @RequestParam(required = false, defaultValue = "0") Integer displayOrder,
            @RequestParam(required = false) String altText,
            Principal principal) {
        try {
            VariantImageDTO imageDTO = variantImageService.uploadVariantImage(
                    variantId, file, displayOrder, altText);
            return ResponseEntity.ok(new ResponseDTO<>(200, "Success", "Image uploaded successfully", imageDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(400, "BAD_REQUEST", "Invalid request: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResponseDTO<>(500, "INTERNAL_SERVER_ERROR", "Failed to upload image: " + e.getMessage(), null));
        }
    }

    /**
     * Get all images for a variant
     * GET /api/seller/products/{productId}/variants/{variantId}/images
     */
    @GetMapping("/{productId}/variants/{variantId}/images")
    public ResponseEntity<ResponseDTO<List<VariantImageDTO>>> getVariantImages(
            @PathVariable Long productId,
            @PathVariable Long variantId) {
        try {
            List<VariantImageDTO> images = variantImageService.getVariantImages(variantId);
            return ResponseEntity.ok(new ResponseDTO<>(200, "Success", "Retrieved successfully", images));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResponseDTO<>(500, "INTERNAL_SERVER_ERROR", "Failed to fetch images: " + e.getMessage(), null));
        }
    }

    /**
     * Get active images for a variant
     * GET /api/seller/products/{productId}/variants/{variantId}/images/active
     */
    @GetMapping("/{productId}/variants/{variantId}/images/active")
    public ResponseEntity<ResponseDTO<List<VariantImageDTO>>> getActiveVariantImages(
            @PathVariable Long productId,
            @PathVariable Long variantId) {
        try {
            List<VariantImageDTO> images = variantImageService.getActiveVariantImages(variantId);
            return ResponseEntity.ok(new ResponseDTO<>(200, "Success", "Retrieved successfully", images));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResponseDTO<>(500, "INTERNAL_SERVER_ERROR", "Failed to fetch images: " + e.getMessage(), null));
        }
    }

    /**
     * Get the main image (displayOrder = 0) for a variant
     * GET /api/seller/products/{productId}/variants/{variantId}/images/main
     */
    @GetMapping("/{productId}/variants/{variantId}/images/main")
    public ResponseEntity<ResponseDTO<VariantImageDTO>> getMainVariantImage(
            @PathVariable Long productId,
            @PathVariable Long variantId) {
        try {
            VariantImageDTO mainImage = variantImageService.getMainImage(variantId);
            if (mainImage == null) {
                return ResponseEntity.status(404)
                        .body(new ResponseDTO<>(404, "NOT_FOUND", "Main image not found", null));
            }
            return ResponseEntity.ok(new ResponseDTO<>(200, "Success", "Retrieved successfully", mainImage));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResponseDTO<>(500, "INTERNAL_SERVER_ERROR", "Failed to fetch main image: " + e.getMessage(), null));
        }
    }

    /**
     * Update variant image metadata (displayOrder and altText)
     * PUT /api/seller/products/{productId}/variants/{variantId}/images/{imageId}
     */
    @PutMapping("/{productId}/variants/{variantId}/images/{imageId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<VariantImageDTO>> updateVariantImage(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @PathVariable Long imageId,
            @RequestParam(required = false) Integer displayOrder,
            @RequestParam(required = false) String altText,
            Principal principal) {
        try {
            VariantImageDTO updatedImage = variantImageService.updateVariantImage(
                    variantId, imageId, displayOrder, altText);
            return ResponseEntity.ok(new ResponseDTO<>(200, "Success", "Image updated successfully", updatedImage));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(400, "BAD_REQUEST", "Invalid request: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResponseDTO<>(500, "INTERNAL_SERVER_ERROR", "Failed to update image: " + e.getMessage(), null));
        }
    }

    /**
     * Delete a variant image
     * DELETE /api/seller/products/{productId}/variants/{variantId}/images/{imageId}
     */
    @DeleteMapping("/{productId}/variants/{variantId}/images/{imageId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<String>> deleteVariantImage(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @PathVariable Long imageId,
            Principal principal) {
        try {
            variantImageService.deleteVariantImage(variantId, imageId);
            return ResponseEntity.ok(new ResponseDTO<>(200, "Success", "Image deleted successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(400, "BAD_REQUEST", "Invalid request: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResponseDTO<>(500, "INTERNAL_SERVER_ERROR", "Failed to delete image: " + e.getMessage(), null));
        }
    }

    /**
     * Reorder variant images
     * POST /api/seller/products/{productId}/variants/{variantId}/images/reorder
     * Body: { "imageIds": [id1, id2, id3, ...] }
     */
    @PostMapping("/{productId}/variants/{variantId}/images/reorder")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<List<VariantImageDTO>>> reorderVariantImages(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @RequestBody Map<String, List<Long>> body,
            Principal principal) {
        try {
            List<Long> imageIds = body.get("imageIds");
            if (imageIds == null || imageIds.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseDTO<>(400, "BAD_REQUEST", "imageIds list is required", null));
            }
            List<VariantImageDTO> reorderedImages = variantImageService.reorderVariantImages(
                    variantId, imageIds);
            return ResponseEntity.ok(new ResponseDTO<>(200, "Success", "Images reordered successfully", reorderedImages));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(400, "BAD_REQUEST", "Invalid request: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResponseDTO<>(500, "INTERNAL_SERVER_ERROR", "Failed to reorder images: " + e.getMessage(), null));
        }
    }

    /**
     * Delete all images for a variant
     * DELETE /api/seller/products/{productId}/variants/{variantId}/images
     */
    @DeleteMapping("/{productId}/variants/{variantId}/images")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<String>> deleteAllVariantImages(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            Principal principal) {
        try {
            variantImageService.deleteAllVariantImages(variantId);
            return ResponseEntity.ok(new ResponseDTO<>(200, "Success", "All images deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResponseDTO<>(500, "INTERNAL_SERVER_ERROR", "Failed to delete images: " + e.getMessage(), null));
        }
    }

}