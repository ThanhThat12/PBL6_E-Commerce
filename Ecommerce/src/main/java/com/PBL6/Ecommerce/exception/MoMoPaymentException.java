package com.PBL6.Ecommerce.exception;

public class MoMoPaymentException extends RuntimeException {
    
    private Integer resultCode;
    private String errorCode;

    public MoMoPaymentException(String message) {
        super(message);
    }

    public MoMoPaymentException(String message, Throwable cause) {
        super(message, cause);
    }

    public MoMoPaymentException(String message, Integer resultCode) {
        super(message);
        this.resultCode = resultCode;
    }

    public MoMoPaymentException(String message, String errorCode, Integer resultCode) {
        super(message);
        this.errorCode = errorCode;
        this.resultCode = resultCode;
    }

    public Integer getResultCode() {
        return resultCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
