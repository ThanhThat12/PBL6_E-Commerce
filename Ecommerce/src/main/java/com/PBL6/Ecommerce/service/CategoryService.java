package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.repository.CategoryRepository;
import com.PBL6.Ecommerce.domain.dto.CategoryDTO;
import com.PBL6.Ecommerce.domain.Category;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
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

}
