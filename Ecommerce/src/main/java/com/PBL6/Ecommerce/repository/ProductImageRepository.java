package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductId(Long productId);
    List<ProductImage> findByProductIdAndColor(Long productId, String color);
    void deleteByProductId(Long productId);
}