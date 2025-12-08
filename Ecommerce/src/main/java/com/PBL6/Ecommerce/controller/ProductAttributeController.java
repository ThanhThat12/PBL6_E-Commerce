package com.PBL6.Ecommerce.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.ProductAttribute;
import com.PBL6.Ecommerce.repository.ProductAttributeRepository;

/**
 * Controller for managing product attributes (Color, Size, Material, etc.)
 * Used by frontend to dynamically load available classification types
 */
@Tag(name = "Product Attributes", description = "Product variant attributes management")
@RestController
@RequestMapping("/api/product-attributes")
public class ProductAttributeController {

    @Autowired
    private ProductAttributeRepository productAttributeRepository;

    /**
     * Get all product attributes
     * @return List of product attributes with id and name
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllAttributes() {
        List<ProductAttribute> attributes = productAttributeRepository.findAll();
        
        List<Map<String, Object>> result = attributes.stream()
            .map(attr -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", attr.getId());
                map.put("name", attr.getName());
                return map;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }

    /**
     * Get a specific product attribute by ID
     * @param id Attribute ID
     * @return Product attribute details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAttributeById(@PathVariable Long id) {
        return productAttributeRepository.findById(id)
            .map(attr -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", attr.getId());
                map.put("name", attr.getName());
                return ResponseEntity.ok(map);
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
