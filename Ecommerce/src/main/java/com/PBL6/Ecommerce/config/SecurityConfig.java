// ...existing code...
package com.PBL6.Ecommerce.config;

import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.http.HttpMethod; // <-- added import

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
            .cors(cors -> cors.and())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/api/auth/login",
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
                    "/api/users/*/addresses/**"
                ).permitAll()

                // Allow unauthenticated GET for the products collection
                .requestMatchers(HttpMethod.GET, "/api/products").permitAll()

                // Product public patterns (single item, search, category)
                .requestMatchers("/api/products/all",
                                 "/api/products/search",
                                 "/api/products/*",
                                 "/api/products/category/**").permitAll()

                // Protect creating products (POST) for ADMIN/SELLER
                .requestMatchers(HttpMethod.POST, "/api/products").hasAnyRole("ADMIN", "SELLER")

                // Category endpoints
                .requestMatchers("/api/categories/addCategory").hasRole("ADMIN")
                .requestMatchers("/api/categories/**").permitAll()

                // Review endpoints
                .requestMatchers(HttpMethod.GET, "/api/products/*/reviews").permitAll() // Public: view reviews
                .requestMatchers(HttpMethod.POST, "/api/products/*/reviews").hasRole("BUYER") // Create review
                .requestMatchers(HttpMethod.PUT, "/api/reviews/*").hasRole("BUYER") // Update review
                .requestMatchers(HttpMethod.DELETE, "/api/reviews/*").hasRole("BUYER") // Delete review
                .requestMatchers(HttpMethod.GET, "/api/reviews/my").hasRole("BUYER") // My reviews
                .requestMatchers("/api/seller/reviews/**").hasRole("SELLER") // Seller review management

                // Profile endpoints (Buyer/Seller)
                .requestMatchers(HttpMethod.GET, "/api/profile").hasAnyRole("BUYER", "SELLER")
                .requestMatchers(HttpMethod.PUT, "/api/profile").hasAnyRole("BUYER", "SELLER")
                .requestMatchers(HttpMethod.POST, "/api/profile/**").hasAnyRole("BUYER", "SELLER")

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