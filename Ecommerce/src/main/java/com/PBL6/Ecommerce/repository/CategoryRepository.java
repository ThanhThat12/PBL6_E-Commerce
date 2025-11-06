package com.PBL6.Ecommerce.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.PBL6.Ecommerce.domain.Category;

import java.util.List;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT DISTINCT c FROM Category c " +
           "JOIN c.products p " +
           "WHERE p.shop.id = :shopId " +
           "ORDER BY c.name ASC")
    List<Category> findCategoriesByShopId(@Param("shopId") Long shopId);

    boolean existsByName(String name);
    // SQL tương đương: SELECT COUNT(*) > 0 FROM categories WHERE name = ?
}
