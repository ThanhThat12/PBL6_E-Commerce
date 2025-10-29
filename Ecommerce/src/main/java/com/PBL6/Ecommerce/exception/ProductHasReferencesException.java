package com.PBL6.Ecommerce.exception;

public class ProductHasReferencesException extends RuntimeException {
    public ProductHasReferencesException(String message) {
        super(message);
    }

    public ProductHasReferencesException(String message, Throwable cause) {
        super(message, cause);
    }
}
