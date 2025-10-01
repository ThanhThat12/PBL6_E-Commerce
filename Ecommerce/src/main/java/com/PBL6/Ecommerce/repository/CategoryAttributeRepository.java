package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.CategoryAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute, Long> {
    List<CategoryAttribute> findByCategoryId(Long categoryId);
}
