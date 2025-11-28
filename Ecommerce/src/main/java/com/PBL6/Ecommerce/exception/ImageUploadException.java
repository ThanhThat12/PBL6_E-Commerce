package com.PBL6.Ecommerce.exception;

/**
 * Exception thrown when image upload operation fails
 */
public class ImageUploadException extends RuntimeException {
    
    public ImageUploadException(String message) {
        super(message);
    }

    public ImageUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
