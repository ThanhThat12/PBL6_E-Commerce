package com.PBL6.Ecommerce.exception;

public class DuplicateCategoryException extends RuntimeException {
    public DuplicateCategoryException(String categoryName) {
        super("Tên danh mục đã tồn tại: " + categoryName);
    }
}
