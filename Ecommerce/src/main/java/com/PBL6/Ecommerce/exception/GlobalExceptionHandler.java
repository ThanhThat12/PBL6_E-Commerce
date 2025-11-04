package com.PBL6.Ecommerce.exception;

/*
 * DEPRECATED - DO NOT USE
 * 
 * This file is REPLACED by:
 * /src/main/java/com/PBL6/Ecommerce/controller/GlobalExceptionHandler.java
 * 
 * Spring Boot detected a bean name conflict when scanning both classes.
 * SOLUTION: All exception handlers have been merged into the controller version.
 * 
 * Delete this file in your next commit.
 * 
 * The working version is at:
 * src/main/java/com/PBL6/Ecommerce/controller/GlobalExceptionHandler.java
 */

// FILE REMOVED - USE CONTROLLER VERSION INSTEAD

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.exception.AdminAccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AdminAccessDeniedException.class)
    public ResponseEntity<ResponseDTO<Void>> handleAdminAccessDenied(AdminAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            new ResponseDTO<>(403, ex.getMessage(), "Truy cập bị từ chối", null)
        );
    }
}
