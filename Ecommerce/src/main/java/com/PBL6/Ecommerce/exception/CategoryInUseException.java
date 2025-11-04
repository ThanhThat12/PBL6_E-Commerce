package com.PBL6.Ecommerce.exception;

public class CategoryInUseException extends RuntimeException {
    public CategoryInUseException(Long categoryId, long productCount) {
        super("Không thể xóa danh mục ID " + categoryId + " đang có " + productCount + " sản phẩm");
    }
}
