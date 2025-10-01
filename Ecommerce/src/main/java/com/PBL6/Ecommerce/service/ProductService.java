package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.domain.dto.ProductDTO;
import com.PBL6.Ecommerce.domain.Product;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<ProductDTO> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(this::mapToDTO);
    }

    public Page<ProductDTO> getProductsByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findByCategoryId(categoryId, pageable).map(this::mapToDTO);
    }

    public Page<ProductDTO> getProductsByShop(Long shopId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findByShopId(shopId, pageable).map(this::mapToDTO);
    }

    public List<ProductDTO> getAllProductsNoPaging() {
        return productRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private ProductDTO mapToDTO(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getImage(),
                product.getPrice(),
                product.getStock(),
                product.getCondition().name(),
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getShop() != null ? product.getShop().getName() : null
        );
    }
}
