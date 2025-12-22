package com.PBL6.Ecommerce.controller.product;

import java.math.BigDecimal;
import java.util.List;

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

import com.PBL6.Ecommerce.domain.dto.ProductCardDTO;
import com.PBL6.Ecommerce.domain.dto.ProductCreateDTO;
import com.PBL6.Ecommerce.domain.dto.ProductDTO;
import com.PBL6.Ecommerce.domain.dto.ProductUpdateDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management APIs - CRUD, search, filter by category/price")
public class ProductController {
    
    @Autowired
    private ProductService productService;



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
            @RequestParam(defaultValue = "20") int size,
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
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductDTO> products = productService.searchActiveProducts(
            name, categoryId, shopId, minPrice, maxPrice, minRating, pageable);
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
    
    // Lấy sản phẩm theo shop - Công khai (cho khách hàng xem shop)
    @Operation(
        summary = "Lấy sản phẩm của shop",
        description = "API công khai để khách hàng xem các sản phẩm đang hoạt động của một shop cụ thể (chỉ trả về thông tin cơ bản)"
    )
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<ResponseDTO<Page<ProductCardDTO>>> getProductsByShop(
            @Parameter(description = "ID của shop") @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
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
        
        Page<ProductCardDTO> products = productService.getActiveProductsByShop(shopId, pageable);
        return ResponseDTO.success(products, "Lấy sản phẩm của shop thành công");
    }
    

    // Cập nhật sản phẩm - Admin hoặc seller sở hữu sản phẩm
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<ProductDTO>> updateProduct(
            @PathVariable Long id, 
            @Valid @RequestBody ProductUpdateDTO request,
            Authentication authentication) {
        ProductDTO product = productService.updateProduct(id, request, authentication);
        return ResponseDTO.success(product, "Cập nhật sản phẩm thành công");
    }

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
    
    // Lấy sản phẩm của seller hiện tại (có phân trang)
    @GetMapping("/my-products")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<Page<ProductDTO>>> getMyProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductDTO> products = productService.getSellerProducts(pageable, authentication);
        return ResponseDTO.success(products, "Lấy sản phẩm của seller thành công");
    }
    
    // Lấy sản phẩm của seller hiện tại (không phân trang)
    @GetMapping("/my-products/all")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<List<ProductDTO>>> getMyProductsList(Authentication authentication) {
        List<ProductDTO> products = productService.getSellerProductsList(authentication);
        return ResponseDTO.success(products, "Lấy tất cả sản phẩm của seller thành công");
    }
    
    // Thay đổi trạng thái sản phẩm - Admin 
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<ProductDTO>> toggleProductStatus(
            @PathVariable Long id,
            Authentication authentication) {
        ProductDTO product = productService.toggleProductStatus(id, authentication);
        return ResponseDTO.success(product, "Thay đổi trạng thái sản phẩm thành công");
    }
    
 // Lấy tất cả sản phẩm của shop của user hiện tại (có phân trang)
    @GetMapping("/my-shop/all")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<ProductDTO>>> getMyShopProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Boolean isActive,
            Authentication authentication) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ProductDTO> products = productService.getMyShopProducts(authentication, isActive, pageable);
            ResponseDTO<Page<ProductDTO>> response = new ResponseDTO<>(200, null, "Lấy sản phẩm của shop thành công", products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<Page<ProductDTO>> response = new ResponseDTO<>(400, "BAD_REQUEST", "Lỗi khi lấy sản phẩm: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }

 @GetMapping("/my-shop/approved")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<ProductDTO>>> getMyShopApprovedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ProductDTO> products = productService.getMyShopApprovedProducts(authentication, pageable);
            ResponseDTO<Page<ProductDTO>> response = new ResponseDTO<>(200, null, "Lấy sản phẩm đã duyệt thành công", products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<Page<ProductDTO>> response = new ResponseDTO<>(400, "BAD_REQUEST", "Lỗi: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 🔧 Recalculate and persist product rating (manual trigger)
    @PostMapping("/{id}/rating/recalculate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<ProductDTO>> recalculateRating(@PathVariable Long id) {
        try {
            productService.updateProductRating(id);
            ProductDTO product = productService.getProductById(id);
            return ResponseDTO.success(product, "Cập nhật lại rating thành công");
        } catch (Exception e) {
            return ResponseDTO.badRequest("Lỗi khi cập nhật rating: " + e.getMessage());
        }
    }

    
}