package com.PBL6.Ecommerce.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.PBL6.Ecommerce.domain.entity.product.Category;
import com.PBL6.Ecommerce.domain.dto.admin.AdminCategoryDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminCategoryStatsDTO;

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
    
    /**
     * API A: Get all categories with statistics for admin
     * - totalProducts: count of products with is_active = true
     * - totalSoldProducts: sum of quantities from COMPLETED orders
     */
    @Query("SELECT new com.PBL6.Ecommerce.domain.dto.admin.AdminCategoryDTO(" +
           "c.id, " +
           "c.name, " +
           "COUNT(DISTINCT CASE WHEN p.isActive = true THEN p.id END), " +
           "COALESCE(SUM(CASE WHEN o.status = 'COMPLETED' THEN oi.quantity ELSE 0 END), 0)) " +
           "FROM Category c " +
           "LEFT JOIN c.products p " +
           "LEFT JOIN OrderItem oi ON oi.productId = p.id " +
           "LEFT JOIN oi.order o " +
           "GROUP BY c.id, c.name " +
           "ORDER BY c.name ASC")
    List<AdminCategoryDTO> findAllCategoriesForAdmin();
    
    /**
     * API B: Get overall statistics for all categories
     * Returns: totalCategories, totalProducts (active), productsSold (from COMPLETED orders)
     */
    @Query("SELECT new com.PBL6.Ecommerce.domain.dto.admin.AdminCategoryStatsDTO(" +
           "COUNT(DISTINCT c.id), " +
           "COUNT(DISTINCT CASE WHEN p.isActive = true THEN p.id END), " +
           "COALESCE(SUM(CASE WHEN o.status = 'COMPLETED' THEN oi.quantity ELSE 0 END), 0)) " +
           "FROM Category c " +
           "LEFT JOIN c.products p " +
           "LEFT JOIN OrderItem oi ON oi.productId = p.id " +
           "LEFT JOIN oi.order o")
    AdminCategoryStatsDTO getCategoryStats();
    
    /**
     * ADMIN Get categories with sales statistics for dashboard
     */
    @Query("SELECT new com.PBL6.Ecommerce.domain.dto.admin.AdminCategoryStatsDTO(" +
           "c.name, " +
           "COALESCE(SUM(CASE WHEN o.status = 'COMPLETED' THEN o.totalAmount ELSE 0 END), 0), " +
           "COUNT(DISTINCT CASE WHEN o.status = 'COMPLETED' THEN o.id END)) " +
           "FROM Category c " +
           "LEFT JOIN c.products p " +
           "LEFT JOIN OrderItem oi ON oi.productId = p.id " +
           "LEFT JOIN oi.order o " +
           "GROUP BY c.id, c.name " +
           "ORDER BY SUM(CASE WHEN o.status = 'COMPLETED' THEN o.totalAmount ELSE 0 END) DESC")
    List<AdminCategoryStatsDTO> getCategoriesWithStats();
}
