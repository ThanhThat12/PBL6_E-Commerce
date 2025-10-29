package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.domain.dto.ProductCreateDTO;
import com.PBL6.Ecommerce.domain.dto.ProductDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")

public class ProductController {
    
    @Autowired
    private ProductService productService;

    // Lấy tất cả sản phẩm công khai (không phân trang)
    @GetMapping("/all")
    public ResponseEntity<ResponseDTO<List<ProductDTO>>> getAllProducts() {
        try {
            List<ProductDTO> products = productService.getAllProducts();
            ResponseDTO<List<ProductDTO>> response = new ResponseDTO<>(200, null, "Lấy danh sách sản phẩm thành công", products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<List<ProductDTO>> response = new ResponseDTO<>(400, "BAD_REQUEST", "Lỗi khi lấy danh sách sản phẩm: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Tạo sản phẩm mới - Chỉ admin hoặc seller
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<ProductDTO>> createProduct(
            @RequestBody ProductCreateDTO request,
            Authentication authentication) {
        try {
            ProductDTO product = productService.createProduct(request, authentication);
            ResponseDTO<ProductDTO> response = new ResponseDTO<>(201, null, "Tạo sản phẩm thành công", product);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<ProductDTO> response = new ResponseDTO<>(400, "BAD_REQUEST", "Lỗi khi tạo sản phẩm: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
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
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ProductDTO> products = productService.getAllProductsForManagement(pageable, authentication);
            ResponseDTO<Page<ProductDTO>> response = new ResponseDTO<>(200, null, "Lấy danh sách sản phẩm quản lý thành công", products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<Page<ProductDTO>> response = new ResponseDTO<>(400, "BAD_REQUEST", "Lỗi khi lấy danh sách sản phẩm quản lý: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Lấy tất cả sản phẩm công khai (có phân trang)
    @GetMapping
    public ResponseEntity<ResponseDTO<Page<ProductDTO>>> getAllActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ProductDTO> products = productService.getAllActiveProducts(pageable);
            ResponseDTO<Page<ProductDTO>> response = new ResponseDTO<>(200, null, "Lấy danh sách sản phẩm hoạt động thành công", products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<Page<ProductDTO>> response = new ResponseDTO<>(400, "BAD_REQUEST", "Lỗi khi lấy danh sách sản phẩm hoạt động: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Lấy sản phẩm theo ID - Công khai
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<ProductDTO>> getProductById(@PathVariable Long id) {
        try {
            ProductDTO product = productService.getProductById(id);
            ResponseDTO<ProductDTO> response = new ResponseDTO<>(200, null, "Lấy thông tin sản phẩm thành công", product);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<ProductDTO> response = new ResponseDTO<>(404, "NOT_FOUND", "Không tìm thấy sản phẩm: " + e.getMessage(), null);
            return ResponseEntity.status(404).body(response);
        }
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
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ProductDTO> products = productService.searchActiveProducts(
                name, categoryId, shopId, minPrice, maxPrice, pageable);
            ResponseDTO<Page<ProductDTO>> response = new ResponseDTO<>(200, null, "Tìm kiếm sản phẩm thành công", products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<Page<ProductDTO>> response = new ResponseDTO<>(400, "BAD_REQUEST", "Lỗi khi tìm kiếm sản phẩm: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Lấy sản phẩm theo category - Công khai (có phân trang)
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ResponseDTO<Page<ProductDTO>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductDTO> products = productService.getActiveProductsByCategory(categoryId, pageable);
            ResponseDTO<Page<ProductDTO>> response = new ResponseDTO<>(200, null, "Lấy sản phẩm theo danh mục thành công", products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<Page<ProductDTO>> response = new ResponseDTO<>(400, "BAD_REQUEST", "Lỗi khi lấy sản phẩm theo danh mục: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Lấy sản phẩm theo category - Công khai (không phân trang)
    @GetMapping("/category/{categoryId}/all")
    public ResponseEntity<ResponseDTO<List<ProductDTO>>> getAllProductsByCategory(@PathVariable Long categoryId) {
        try {
            List<ProductDTO> products = productService.getProductsByCategory(categoryId);
            ResponseDTO<List<ProductDTO>> response = new ResponseDTO<>(200, null, "Lấy tất cả sản phẩm theo danh mục thành công", products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<List<ProductDTO>> response = new ResponseDTO<>(400, "BAD_REQUEST", "Lỗi khi lấy sản phẩm theo danh mục: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
 
    // Xóa sản phẩm - Admin hoặc seller sở hữu sản phẩm
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @productService.isProductOwner(#id, authentication.name))")
    public ResponseEntity<ResponseDTO<Void>> deleteProduct(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            productService.deleteProduct(id, authentication);
            ResponseDTO<Void> response = new ResponseDTO<>(200, null, "Xóa sản phẩm thành công", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<Void> response = new ResponseDTO<>(400, "BAD_REQUEST", "Lỗi khi xóa sản phẩm: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Lấy sản phẩm của seller hiện tại (có phân trang)
    @GetMapping("/my-products")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<Page<ProductDTO>>> getMyProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ProductDTO> products = productService.getSellerProducts(pageable, authentication);
            ResponseDTO<Page<ProductDTO>> response = new ResponseDTO<>(200, null, "Lấy sản phẩm của seller thành công", products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<Page<ProductDTO>> response = new ResponseDTO<>(403, "FORBIDDEN", "Lỗi khi lấy sản phẩm của seller: " + e.getMessage(), null);
            return ResponseEntity.status(403).body(response);
        }
    }
    
    // Lấy sản phẩm của seller hiện tại (không phân trang)
    @GetMapping("/my-products/all")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<List<ProductDTO>>> getMyProductsList(Authentication authentication) {
        try {
            List<ProductDTO> products = productService.getSellerProductsList(authentication);
            ResponseDTO<List<ProductDTO>> response = new ResponseDTO<>(200, null, "Lấy tất cả sản phẩm của seller thành công", products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<List<ProductDTO>> response = new ResponseDTO<>(403, "FORBIDDEN", "Lỗi khi lấy sản phẩm của seller: " + e.getMessage(), null);
            return ResponseEntity.status(403).body(response);
        }
    }
    
    // Thay đổi trạng thái sản phẩm - Admin hoặc seller sở hữu
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<ProductDTO>> toggleProductStatus(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            ProductDTO product = productService.toggleProductStatus(id, authentication);
            ResponseDTO<ProductDTO> response = new ResponseDTO<>(200, null, "Thay đổi trạng thái sản phẩm thành công", product);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<ProductDTO> response = new ResponseDTO<>(400, "BAD_REQUEST", "Lỗi khi thay đổi trạng thái sản phẩm: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }
}