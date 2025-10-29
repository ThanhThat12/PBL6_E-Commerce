package com.PBL6.Ecommerce.util;

import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 1. Lấy JWT token từ request header
            String jwt = getJwtFromRequest(request);
            
            if (jwt != null && tokenProvider.validateToken(jwt)) {
                // 2. Lấy username từ token
                String username = tokenProvider.getUsernameFromToken(jwt);
                
                // 3. Load user từ database
                Optional<User> userOpt = userRepository.findOneByUsername(username);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    
                    // 4. Kiểm tra user đã activate
                    if (!user.isActivated()) {
                        System.out.println("⚠️  DEBUG - User not activated: " + username);
                        filterChain.doFilter(request, response);
                        return;
                    }
                    
                    // 5. Tạo authorities từ user role
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
                    
                    // 6. Tạo authentication object
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(username, null, Collections.singletonList(authority));
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 7. Set vào SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    System.out.println("🔍 DEBUG - JWT Auth successful for user: " + username + 
                                     " with role: ROLE_" + user.getRole().name());
                } else {
                    System.out.println("❌ DEBUG - User not found in database: " + username);
                }
            } else {
                if (jwt != null) {
                    System.out.println("❌ DEBUG - Invalid JWT token");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ JWT Authentication error: " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        System.out.println("🔍 DEBUG - Authorization header: " + bearerToken);
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7); // Remove "Bearer " prefix
            System.out.println("🔍 DEBUG - Extracted JWT token: " + token.substring(0, Math.min(20, token.length())) + "...");
            return token;
        }
        
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // Skip filter cho public endpoints
        return path.startsWith("/api/auth/") ||
               path.startsWith("/api/register/") ||
               path.startsWith("/api/forgot-password/") ||
               path.equals("/api/products/search") ||
               path.matches("/api/products/\\d+") ||
               path.startsWith("/api/products/category/") ||
               path.startsWith("/api/categories") ||
               path.equals("/error") ||
               path.equals("/favicon.ico");
    }
}
