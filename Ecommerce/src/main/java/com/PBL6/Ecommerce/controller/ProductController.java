package com.PBL6.Ecommerce.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import com.PBL6.Ecommerce.service.ProductService;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.ProductDTO;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<Page<ProductDTO>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ProductDTO> result = productService.getAllProducts(page, size);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Success", result));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ResponseDTO<Page<ProductDTO>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ProductDTO> result = productService.getProductsByCategory(categoryId, page, size);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Success", result));
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<ResponseDTO<Page<ProductDTO>>> getProductsByShop(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ProductDTO> result = productService.getProductsByShop(shopId, page, size);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Success", result));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductDTO>> getAllProductsNoPaging() {
        List<ProductDTO> products = productService.getAllProductsNoPaging();
        return ResponseEntity.ok(products);
    }
}
