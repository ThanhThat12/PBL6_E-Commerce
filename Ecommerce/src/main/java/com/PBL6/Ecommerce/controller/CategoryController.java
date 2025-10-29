package com.PBL6.Ecommerce.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.PBL6.Ecommerce.service.CategoryService;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.CategoryDTO;
import com.PBL6.Ecommerce.domain.dto.ProductDTO;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 📋 API 1: GET /api/categories
     * Public - Lấy tất cả categories
     */
    @GetMapping
    public ResponseEntity<ResponseDTO<?>> getCategories() {
        try {
            var data = categoryService.getAllCategories();
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Lấy danh mục thành công", data)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Thất bại", null)
            );
        }
    }

    /**
     * ➕ API 2: POST /api/categories
     * Admin only - Tạo category mới
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<?>> addCategory(@RequestBody CategoryDTO dto) {
        try {
            // Validate
            if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new ResponseDTO<>(400, "Tên danh mục không được để trống", "Validation failed", null)
                );
            }

            var data = categoryService.addCategory(dto);
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Thêm danh mục thành công", data)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Thất bại", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ResponseDTO<>(500, e.getMessage(), "Lỗi hệ thống", null)
            );
        }
    }

    /**
     * ❌ API 3: DELETE /api/categories/{id}
     * Admin only - Xóa category
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<?>> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Xóa danh mục thành công", "Category ID: " + id + " đã được xóa")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Xóa danh mục thất bại", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ResponseDTO<>(500, e.getMessage(), "Lỗi hệ thống", null)
            );
        }
    }

    /**
     * 🛍️ API 4: GET /api/categories/seller/my-categories
     * Seller only - Lấy tất cả categories mà shop có sản phẩm
     */
    @GetMapping("/seller/my-categories")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<List<CategoryDTO>>> getMyCategories() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            List<CategoryDTO> categories = categoryService.getCategoriesByShop(username);
            
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, 
                    "Lấy danh sách categories của shop thành công", categories)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, "BAD_REQUEST", 
                    "Lỗi khi lấy categories: " + e.getMessage(), null)
            );
        }
    }

    /**
     * 🛍️ API 5: GET /api/categories/seller/my-products/{categoryId}
     * Seller only - Lấy sản phẩm theo category của shop mình
     */
    @GetMapping("/seller/my-products/{categoryId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<List<ProductDTO>>> getMyProductsByCategory(
            @PathVariable Long categoryId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            List<ProductDTO> products = categoryService.getProductsByCategoryAndShop(categoryId, username);
            
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, 
                    "Lấy sản phẩm theo danh mục thành công", products)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, "BAD_REQUEST", 
                    "Lỗi khi lấy sản phẩm: " + e.getMessage(), null)
            );
        }
    }
}
