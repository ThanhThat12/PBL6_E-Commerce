package com.PBL6.Ecommerce.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import com.PBL6.Ecommerce.service.CategoryService;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.CategoryDTO;

@RestController

@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<?>> getCategories() {
        try {
            var data = categoryService.getAllCategories();
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "Lấy danh mục thành công", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseDTO<>(400, e.getMessage(), "Thất bại", null));
        }
    }

    @PostMapping("/addCategory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<?>> addCategory(@RequestBody CategoryDTO dto) {
        try {
            var data = categoryService.addCategory(dto);
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "Thêm danh mục thành công", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseDTO<>(400, e.getMessage(), "Thất bại", null));
        }
    }

    @GetMapping("/{id}/attributes")
    public ResponseEntity<ResponseDTO<?>> getAttributes(@PathVariable Long id) {
        try {
            var data = categoryService.getAttributesByCategory(id);
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "Lấy thuộc tính thành công", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseDTO<>(400, e.getMessage(), "Thất bại", null));
        }
    }
}
