package com.PBL6.Ecommerce.exception;

/**
 * Exception thrown when requested image is not found
 */
public class ImageNotFoundException extends RuntimeException {
    
    public ImageNotFoundException(String message) {
        super(message);
    }

    public ImageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
