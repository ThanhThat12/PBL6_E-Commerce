package com.PBL6.Ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/register/*", "/api/forgot-password/**", "/api/authenticate", "/api/authenticate/**", "/api/products/**").permitAll()
                .requestMatchers("/api/categories/addCategory").hasRole("ADMIN") 
                .requestMatchers("/api/categories/**").permitAll() 
                .anyRequest().authenticated() 
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());
        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(
            new javax.crypto.spec.SecretKeySpec(
                "my-secret-key-which-should-be-long".getBytes(), "HmacSHA256"
            )
        ).build();
}
}