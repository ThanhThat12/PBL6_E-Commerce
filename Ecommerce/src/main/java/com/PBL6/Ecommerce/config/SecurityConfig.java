package com.PBL6.Ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configure(http)) // Enable CORS support (Spring Security 6.1+)
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - most specific first
                .requestMatchers(
                    "/api/auth/**",
                    "/api/register/**",
                    "/api/forgot-password/**",
                    "/api/authenticate",
                    "/api/authenticate/**",
                    "/api/products/**",
                    "/api/debug/**"
                ).permitAll()
                
                // Product endpoints - specific to general
                .requestMatchers("/api/products/search").permitAll()
                .requestMatchers("/api/products/{id}").permitAll()
                .requestMatchers("/api/products/category/**").permitAll()
                .requestMatchers("/api/products").hasAnyRole("ADMIN", "SELLER")
                
                // Category endpoints
                .requestMatchers("/api/categories/addCategory").hasRole("ADMIN")
                .requestMatchers("/api/categories/**").permitAll()
                
                // Cart endpoints - require authentication
                .requestMatchers("/api/cart/**").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder()))
            );
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public JwtDecoder jwtDecoder() {
        // Validate secret key length (must be at least 32 chars for HS256)
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalArgumentException(
                "JWT secret must be at least 32 characters long for HS256 algorithm"
            );
        }
        
        return NimbusJwtDecoder.withSecretKey(
            new javax.crypto.spec.SecretKeySpec(
                jwtSecret.getBytes(), "HmacSHA256"
            )
        ).build();
    }
}