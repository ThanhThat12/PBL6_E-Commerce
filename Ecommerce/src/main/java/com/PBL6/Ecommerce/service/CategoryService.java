package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.repository.CategoryRepository;
import com.PBL6.Ecommerce.repository.CategoryAttributeRepository;
import com.PBL6.Ecommerce.domain.dto.CategoryDTO;
import com.PBL6.Ecommerce.domain.dto.AttributeDTO;
import com.PBL6.Ecommerce.domain.Category;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryAttributeRepository categoryAttributeRepository;

    public CategoryService(CategoryRepository categoryRepository,
            CategoryAttributeRepository categoryAttributeRepository) {
        this.categoryRepository = categoryRepository;
        this.categoryAttributeRepository = categoryAttributeRepository;
    }

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(c -> new CategoryDTO(c.getId(), c.getName()))
                .toList();
    }

    public CategoryDTO addCategory(CategoryDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        Category saved = categoryRepository.save(category);
        return new CategoryDTO(saved.getId(), saved.getName());
    }

    public List<AttributeDTO> getAttributesByCategory(Long categoryId) {
        return categoryAttributeRepository.findByCategoryId(categoryId)
                .stream()
                .map(ca -> new AttributeDTO(ca.getAttribute().getId(), ca.getAttribute().getName()))
                .toList();
    }
}
