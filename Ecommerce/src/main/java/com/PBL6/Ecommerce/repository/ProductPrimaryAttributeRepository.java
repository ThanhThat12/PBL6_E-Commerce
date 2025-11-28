package com.PBL6.Ecommerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.PBL6.Ecommerce.domain.ProductPrimaryAttribute;

/**
 * Repository for ProductPrimaryAttribute entity.
 * Handles queries related to primary attributes that determine variant-specific images.
 */
@Repository
public interface ProductPrimaryAttributeRepository extends JpaRepository<ProductPrimaryAttribute, Long> {
    
    /**
     * Find the primary attribute for a specific product.
     * 
     * @param productId The product ID
     * @return Optional containing ProductPrimaryAttribute if exists
     */
    Optional<ProductPrimaryAttribute> findByProductId(Long productId);
    
    /**
     * Check if a product has a primary attribute defined.
     * 
     * @param productId The product ID
     * @return true if primary attribute exists, false otherwise
     */
    boolean existsByProductId(Long productId);
}
