package com.PBL6.Ecommerce.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.domain.Category;
import com.PBL6.Ecommerce.domain.dto.CategoryDTO;
import com.PBL6.Ecommerce.exception.CategoryInUseException;
import com.PBL6.Ecommerce.exception.CategoryNotFoundException;
import com.PBL6.Ecommerce.exception.DuplicateCategoryException;
import com.PBL6.Ecommerce.repository.CategoryRepository;
import com.PBL6.Ecommerce.repository.ProductRepository;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository,
            ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(c -> new CategoryDTO(c.getId(), c.getName()))
                .toList();
    }

    public CategoryDTO addCategory(CategoryDTO dto) {
        // 1. VALIDATION - Kiểm tra tên đã tồn tại chưa
        if (categoryRepository.existsByName(dto.getName())) {
            throw new DuplicateCategoryException(dto.getName());
        }
        
        // 2. TẠO ENTITY MỚI
        Category category = new Category();
        category.setName(dto.getName());
        
        // 3. LƯU VÀO DATABASE
        Category saved = categoryRepository.save(category);
        // SQL: INSERT INTO categories (name) VALUES (?);
        
        // 4. CONVERT VÀ TRẢ VỀ
        return new CategoryDTO(saved.getId(), saved.getName());
    }

    /**
     * Xóa category - Chỉ admin
     */
    @Transactional
    public void deleteCategory(Long categoryId) {
        // Kiểm tra category có tồn tại không
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        
        // Kiểm tra có products nào đang sử dụng category này không
        long productCount = productRepository.countByCategoryId(categoryId);
        if (productCount > 0) {
            throw new CategoryInUseException(categoryId, productCount);
        }
                
        // Xóa category
        categoryRepository.delete(category);
    }
}
