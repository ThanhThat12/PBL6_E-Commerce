package com.PBL6.Ecommerce.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String message) {
        super(message);
    }
    
    public CategoryNotFoundException(Long categoryId) {
        super("Danh mục không tồn tại với ID: " + categoryId);
    }
}
