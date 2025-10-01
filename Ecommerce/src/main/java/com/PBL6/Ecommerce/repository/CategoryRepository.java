package com.PBL6.Ecommerce.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.PBL6.Ecommerce.domain.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {}
