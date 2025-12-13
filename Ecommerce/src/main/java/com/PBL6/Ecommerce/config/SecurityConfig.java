// ...existing code...
package com.PBL6.Ecommerce.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.http.HttpMethod; // <-- added import
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;



@Configuration
public class SecurityConfig {
    @Value("${jwt.secret}")
    private String jwtSecret;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // converter để lấy claim "roles" và thêm prefix "ROLE_"
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthConverter = new JwtAuthenticationConverter();
        jwtAuthConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

        http
            // .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .cors(cors -> cors.and())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints

                .requestMatchers(
                    "/api/auth/**",
                    "/api/register/**", 
                    "/api/forgot-password/**",

                    "/api/authenticate",
                    "/api/authenticate/**",
                    "/api/authenticate/google",
                    "/api/authenticate/facebook",
                    "/api/refresh-token",
                    "/api/logout",
                    "/api/ghn/master",
                    "/api/ghn/master/**",
                    "/api/users/*/addresses",
                    "/api/users/*/addresses/**",
                    // MoMo Payment callbacks - must be public for MoMo to call
                    "/api/payment/momo/return",
                    "/api/payment/momo/callback",
                    "/api/payment/momo/test-callback",
                    // Wallet deposit callback - must be public for MoMo IPN
                    "/api/wallet/deposit/callback",
                    // WebSocket endpoints - allow SockJS handshake and STOMP connections
                    "/ws/**",
                    "/ws",
                    // Swagger UI endpoints - MUST be public for API documentation access
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/v3/api-docs",
                    "/swagger-resources/**",
                    "/configuration/**",
                    "/webjars/**",
                    "/api-docs/**"
                ).permitAll()
                
                // Checkout endpoints - require authentication except for testing
                .requestMatchers(HttpMethod.POST, "/api/checkout/available-services").permitAll() // For testing
                .requestMatchers(HttpMethod.POST, "/api/checkout/calculate-fee").permitAll() // For testing
                .requestMatchers("/api/checkout/**").authenticated()

                // Allow unauthenticated GET for the products collection (with or without query params)
                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()

                // Search API - public endpoints
                .requestMatchers(HttpMethod.GET, "/api/search/suggestions").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/search/trending").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/search/facets").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/search/shops").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/search/track").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/search/track-click").permitAll()
                // Search history - authenticated endpoints
                .requestMatchers("/api/search/history/**").authenticated()

                // Shop public endpoints
                .requestMatchers(HttpMethod.GET, "/api/shops/*").permitAll()

                // Protect creating products (POST) for ADMIN/SELLER
                .requestMatchers(HttpMethod.POST, "/api/products").hasAnyRole("ADMIN", "SELLER")

                // Category endpoints - public read access
                .requestMatchers(HttpMethod.GET, "/api/categories", "/api/categories/**").permitAll()
                .requestMatchers("/api/categories/addCategory").hasRole("ADMIN")
                
                // Cart endpoints - require authentication
                .requestMatchers("/api/cart/**").authenticated()

                // Product Attributes - public for frontend to load classification types
                .requestMatchers(HttpMethod.GET, "/api/product-attributes").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/product-attributes/*").permitAll()

                // Review endpoints
                .requestMatchers(HttpMethod.GET, "/api/products/*/reviews").permitAll() // Public: view product reviews
                .requestMatchers(HttpMethod.GET, "/api/products/*/rating-summary").permitAll() // Public: rating summary
                .requestMatchers(HttpMethod.GET, "/api/users/*/reviews").permitAll() // Public: user reviews
                .requestMatchers(HttpMethod.GET, "/api/products/*/review-eligibility").hasAnyRole("BUYER", "SELLER") // Check review eligibility
                .requestMatchers(HttpMethod.POST, "/api/products/*/reviews").hasAnyRole("BUYER", "SELLER") // Create review (BUYER or SELLER who bought from other shops)
                .requestMatchers(HttpMethod.PUT, "/api/reviews/*").hasAnyRole("BUYER", "SELLER") // Update review (only 1 time within 30 days)
                .requestMatchers(HttpMethod.DELETE, "/api/reviews/*").hasRole("ADMIN") // Delete review (ADMIN only - users cannot delete reviews)
                .requestMatchers(HttpMethod.GET, "/api/my-reviews").hasAnyRole("BUYER", "SELLER") // My reviews
                .requestMatchers(HttpMethod.POST, "/api/reviews/*/reply").hasRole("SELLER") // Seller reply
                // Review Like/Report endpoints
                .requestMatchers(HttpMethod.POST, "/api/reviews/*/like").hasAnyRole("BUYER", "SELLER") // Toggle like
                .requestMatchers(HttpMethod.GET, "/api/reviews/*/like").permitAll() // Get like status (public)
                .requestMatchers(HttpMethod.POST, "/api/reviews/*/report").hasAnyRole("BUYER", "SELLER") // Report review
                .requestMatchers("/api/admin/reviews/reports/**").hasRole("ADMIN") // Admin report management
                
                // Seller shop reviews management
                .requestMatchers(HttpMethod.GET, "/api/shops/*/reviews").hasRole("SELLER") // Get shop reviews with filters
                .requestMatchers(HttpMethod.GET, "/api/shops/*/reviews/unreplied").hasRole("SELLER") // Get unreplied reviews
                .requestMatchers(HttpMethod.GET, "/api/my-shop/reviews/all").hasRole("SELLER") // Get all shop reviews grouped

                // Profile endpoints (Buyer/Seller) - per spec 006-profile US1
                .requestMatchers(HttpMethod.GET, "/api/profile").hasAnyRole("BUYER", "SELLER")
                .requestMatchers(HttpMethod.GET, "/api/profile/*").permitAll() // Public profile by username
                .requestMatchers(HttpMethod.PUT, "/api/profile").hasAnyRole("BUYER", "SELLER")
                .requestMatchers(HttpMethod.POST, "/api/profile/avatar").hasAnyRole("BUYER", "SELLER")
                .requestMatchers(HttpMethod.DELETE, "/api/profile/avatar").hasAnyRole("BUYER", "SELLER")
                .requestMatchers(HttpMethod.POST, "/api/profile/change-password").hasAnyRole("BUYER", "SELLER")

                // Address endpoints (Buyer/Seller) - per spec 006-profile US2
                .requestMatchers(HttpMethod.GET, "/api/addresses").hasAnyRole("BUYER", "SELLER")
                .requestMatchers(HttpMethod.GET, "/api/addresses/*").hasAnyRole("BUYER", "SELLER")
                .requestMatchers(HttpMethod.POST, "/api/addresses").hasAnyRole("BUYER", "SELLER")
                .requestMatchers(HttpMethod.PUT, "/api/addresses/*").hasAnyRole("BUYER", "SELLER")
                .requestMatchers(HttpMethod.DELETE, "/api/addresses/*").hasAnyRole("BUYER", "SELLER")
                .requestMatchers(HttpMethod.GET, "/api/addresses/auto-fill").hasAnyRole("BUYER", "SELLER")
                
                // GHN Locations proxy (public for address forms) - per spec 006-profile US2
                .requestMatchers(HttpMethod.GET, "/api/locations/**").permitAll()

                // Seller Registration (Buyer upgrade to Seller - Shopee style with Admin approval)
                .requestMatchers(HttpMethod.POST, "/api/seller/register").hasRole("BUYER")
                .requestMatchers(HttpMethod.GET, "/api/seller/registration/status").hasAnyRole("BUYER")
                .requestMatchers(HttpMethod.DELETE, "/api/seller/registration").hasAnyRole("BUYER") // Allow SELLER to cancel REJECTED application
                .requestMatchers(HttpMethod.PUT, "/api/seller/registration").hasRole("BUYER")
                .requestMatchers(HttpMethod.GET, "/api/seller/registration/can-submit").hasRole("BUYER")

                // Admin - Seller Registration Management
                .requestMatchers("/api/admin/seller-registrations/**").hasRole("ADMIN")

                // Seller Shop Management
                .requestMatchers(HttpMethod.GET, "/api/seller/shop").hasRole("SELLER")
                .requestMatchers(HttpMethod.PUT, "/api/seller/shop").hasRole("SELLER")
                .requestMatchers(HttpMethod.GET, "/api/seller/shop/analytics").hasRole("SELLER")
                
                // Seller Orders Management
                .requestMatchers(HttpMethod.GET, "/api/seller/orders").hasRole("SELLER")
                .requestMatchers(HttpMethod.GET, "/api/seller/orders/*").hasRole("SELLER")
                .requestMatchers(HttpMethod.PATCH, "/api/seller/orders/*/status").hasRole("SELLER")
                
                // Seller Analytics
                .requestMatchers(HttpMethod.GET, "/api/seller/analytics/**").hasRole("SELLER")
                
                // Seller Vouchers Management
                .requestMatchers(HttpMethod.POST, "/api/seller/vouchers").hasRole("SELLER")
                .requestMatchers(HttpMethod.GET, "/api/seller/vouchers").hasRole("SELLER")
                .requestMatchers(HttpMethod.GET, "/api/seller/vouchers/active").hasRole("SELLER")
                .requestMatchers(HttpMethod.GET, "/api/seller/vouchers/available").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/seller/vouchers/*/deactivate").hasRole("SELLER")

                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthConverter)
                )
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();

    }
}