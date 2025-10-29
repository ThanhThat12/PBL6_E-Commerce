package com.PBL6.Ecommerce.domain.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseDTO<T> {
    private int status;
    private String error;
    private String message;
    private T data;

    public ResponseDTO(int status, String error, String message, T data) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.data = data;
    }

    // ===== Factory Methods =====
    
    /**
     * Create success response with data and custom message
     */
    public static <T> ResponseEntity<ResponseDTO<T>> success(T data, String message) {
        ResponseDTO<T> response = new ResponseDTO<>(200, null, message, data);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create success response with data and default message
     */
    public static <T> ResponseEntity<ResponseDTO<T>> success(T data) {
        return success(data, "Success");
    }
    
    /**
     * Create created response (201) with data
     */
    public static <T> ResponseEntity<ResponseDTO<T>> created(T data, String message) {
        ResponseDTO<T> response = new ResponseDTO<>(201, null, message, data);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Create no content response (204)
     */
    public static <T> ResponseEntity<ResponseDTO<T>> noContent() {
        ResponseDTO<T> response = new ResponseDTO<>(204, null, "No content", null);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
    
    /**
     * Create bad request response (400)
     */
    public static <T> ResponseEntity<ResponseDTO<T>> badRequest(String message) {
        ResponseDTO<T> response = new ResponseDTO<>(400, "BAD_REQUEST", message, null);
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Create not found response (404)
     */
    public static <T> ResponseEntity<ResponseDTO<T>> notFound(String message) {
        ResponseDTO<T> response = new ResponseDTO<>(404, "NOT_FOUND", message, null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Create error response with custom status and error code
     */
    public static <T> ResponseEntity<ResponseDTO<T>> error(int status, String errorCode, String message) {
        ResponseDTO<T> response = new ResponseDTO<>(status, errorCode, message, null);
        return ResponseEntity.status(status).body(response);
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
    
}
