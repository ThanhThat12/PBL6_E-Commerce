package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {
    Optional<ProductAttribute> findByName(String name);
    boolean existsByName(String name);
}