package com.PBL6.Ecommerce.exception;

/**
 * Exception thrown when image validation fails
 * (format, size, dimensions, content-type)
 */
public class ImageValidationException extends RuntimeException {
    
    public ImageValidationException(String message) {
        super(message);
    }

    public ImageValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
