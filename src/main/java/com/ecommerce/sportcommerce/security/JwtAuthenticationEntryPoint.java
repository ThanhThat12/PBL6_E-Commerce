package com.ecommerce.sportcommerce.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.ecommerce.sportcommerce.dto.response.ApiResponse;
import com.ecommerce.sportcommerce.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT Authentication Entry Point
 * Handles unauthorized access attempts
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);
    
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        
        logger.error("Unauthorized error: {}", authException.getMessage());
        
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Unauthorized - Vui lòng đăng nhập")
                .status(HttpServletResponse.SC_UNAUTHORIZED)
                .error("Unauthorized")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        
        ApiResponse<ErrorResponse> apiResponse = ApiResponse.error(
                "Unauthorized - Vui lòng đăng nhập",
                errorResponse
        );
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules(); // For LocalDateTime serialization
        mapper.writeValue(response.getOutputStream(), apiResponse);
    }
}
