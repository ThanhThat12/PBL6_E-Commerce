package com.PBL6.Ecommerce.exception;

/**
 * Exception thrown when Cloudinary service operations fail
 * (API errors, network issues, authentication failures)
 */
public class CloudinaryServiceException extends RuntimeException {
    
    public CloudinaryServiceException(String message) {
        super(message);
    }

    public CloudinaryServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
