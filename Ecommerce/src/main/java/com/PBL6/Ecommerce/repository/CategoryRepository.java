package com.PBL6.Ecommerce.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.PBL6.Ecommerce.domain.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Spring Data JPA tự động tạo các method:
    // - findAll()
    // - findById(Long id)
    // - save(Category)
    // - delete(Category)
    
    // ✅ Custom method - Kiểm tra tên đã tồn tại chưa
    boolean existsByName(String name);
    // SQL tương đương: SELECT COUNT(*) > 0 FROM categories WHERE name = ?
}
