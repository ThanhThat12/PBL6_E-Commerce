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
                    "/ws"
                ).permitAll()
                
                // Checkout endpoints - require authentication except for testing
                .requestMatchers(HttpMethod.POST, "/api/checkout/available-services").permitAll() // For testing
                .requestMatchers(HttpMethod.POST, "/api/checkout/calculate-fee").permitAll() // For testing
                .requestMatchers("/api/checkout/**").authenticated()

                // Allow unauthenticated GET for the products collection (with or without query params)
                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()

                // Protect creating products (POST) for ADMIN/SELLER
                .requestMatchers(HttpMethod.POST, "/api/products").hasAnyRole("ADMIN", "SELLER")

                // Category endpoints - public read access
                .requestMatchers(HttpMethod.GET, "/api/categories", "/api/categories/**").permitAll()
                .requestMatchers("/api/categories/addCategory").hasRole("ADMIN")
                
                // Cart endpoints - require authentication
                .requestMatchers("/api/cart/**").authenticated()

                // Review endpoints
                .requestMatchers(HttpMethod.GET, "/api/products/*/reviews").permitAll() // Public: view product reviews
                .requestMatchers(HttpMethod.GET, "/api/products/*/rating-summary").permitAll() // Public: rating summary
                .requestMatchers(HttpMethod.GET, "/api/users/*/reviews").permitAll() // Public: user reviews
                .requestMatchers(HttpMethod.POST, "/api/products/*/reviews").hasRole("BUYER") // Create review from product detail page
                .requestMatchers(HttpMethod.PUT, "/api/reviews/*").hasRole("BUYER") // Update review
                .requestMatchers(HttpMethod.DELETE, "/api/reviews/*").hasAnyRole("ADMIN", "BUYER") // Delete review (admin or owner)
                .requestMatchers(HttpMethod.GET, "/api/my-reviews").hasRole("BUYER") // My reviews
                .requestMatchers(HttpMethod.POST, "/api/reviews/*/reply").hasRole("SELLER") // Seller reply
                
                // Seller shop reviews management
                .requestMatchers(HttpMethod.GET, "/api/shops/*/reviews").hasRole("SELLER") // Get shop reviews with filters
                .requestMatchers(HttpMethod.GET, "/api/shops/*/reviews/unreplied").hasRole("SELLER") // Get unreplied reviews
                .requestMatchers(HttpMethod.GET, "/api/my-shop/reviews/all").hasRole("SELLER") // Get all shop reviews grouped

                // Profile endpoints (Buyer/Seller)
                .requestMatchers(HttpMethod.GET, "/api/profile").hasAnyRole("BUYER", "SELLER")
                .requestMatchers(HttpMethod.PUT, "/api/profile").hasAnyRole("BUYER", "SELLER")
                .requestMatchers(HttpMethod.POST, "/api/profile/**").hasAnyRole("BUYER", "SELLER")

                // Seller Registration (Buyer upgrade to Seller - Shopee style)
                .requestMatchers(HttpMethod.POST, "/api/seller/register").hasRole("BUYER")

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