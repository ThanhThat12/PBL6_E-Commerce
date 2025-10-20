package com.PBL6.Ecommerce.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.PBL6.Ecommerce.service.TokenBlacklistService;
import com.PBL6.Ecommerce.util.TokenProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private TokenProvider tokenProvider;
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService; // Prompt 4: Token Revocation

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (tokenProvider.validateJwt(token)) {
                    // Prompt 4: Check if token has been blacklisted (revoked)
                    String jti = tokenProvider.getJtiFromJwt(token);
                    if (tokenBlacklistService.isTokenBlacklisted(jti)) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("{\"error\": \"Token has been revoked\"}");
                        return;
                    }
                    
                    String username = tokenProvider.getUsernameFromJwt(token);
                    request.setAttribute("username", username);
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
