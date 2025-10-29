package com.PBL6.Ecommerce.controller;

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

import com.PBL6.Ecommerce.domain.dto.ProductCreateDTO;
import com.PBL6.Ecommerce.domain.dto.ProductDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
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
    
    // Thêm sản phẩm đơn giản (cho admin)
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<ProductDTO>> addProduct(@Valid @RequestBody ProductCreateDTO request) {
        ProductDTO product = productService.addProduct(request);
        return ResponseDTO.created(product, "Thêm sản phẩm thành công");
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
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @productService.isProductOwner(#id, authentication.name))")
    public ResponseEntity<ResponseDTO<ProductDTO>> updateProduct(
            @PathVariable Long id, 
            @Valid @RequestBody ProductCreateDTO request,
            Authentication authentication) {
        ProductDTO product = productService.updateProduct(id, request, authentication);
        return ResponseDTO.success(product, "Cập nhật sản phẩm thành công");
    }
    
    // Xóa sản phẩm - Admin hoặc seller sở hữu sản phẩm
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @productService.isProductOwner(#id, authentication.name))")
    public ResponseEntity<ResponseDTO<Void>> deleteProduct(
            @PathVariable Long id,
            Authentication authentication) {
        productService.deleteProduct(id, authentication);
        return ResponseDTO.success(null, "Xóa sản phẩm thành công");
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
    
    // Thay đổi trạng thái sản phẩm - Admin hoặc seller sở hữu
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @productService.isProductOwner(#id, authentication.name))")
    public ResponseEntity<ResponseDTO<ProductDTO>> toggleProductStatus(
            @PathVariable Long id,
            Authentication authentication) {
        ProductDTO product = productService.toggleProductStatus(id, authentication);
        return ResponseDTO.success(product, "Thay đổi trạng thái sản phẩm thành công");
    }
}