package com.PBL6.Ecommerce.exception;

public class OtpNotVerifiedException extends RuntimeException {
    public OtpNotVerifiedException(String message) {
        super(message);
    }

    public OtpNotVerifiedException(String message, Throwable cause) {
        super(message, cause);
    }
}
