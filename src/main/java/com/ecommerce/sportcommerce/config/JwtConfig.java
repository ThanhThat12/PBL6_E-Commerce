package com.ecommerce.sportcommerce.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtConfig {
    private String secret = "your-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm";
    private long expiration = 3600000; // 1 hour in milliseconds
    private long refreshExpiration = 86400000; // 24 hours in milliseconds
}
