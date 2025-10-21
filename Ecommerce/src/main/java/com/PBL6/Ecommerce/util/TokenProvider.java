package com.PBL6.Ecommerce.util;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:3600000}")
    private long jwtExpirationMs;
 
    private Key getKey() {
        // Validate secret key meets minimum security requirements (at least 256 bits = 32 characters)
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalArgumentException(
                "JWT secret key must be at least 32 characters long (256 bits) for HS256 algorithm. " +
                "Current length: " + (jwtSecret != null ? jwtSecret.length() : 0)
            );
        }
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Create JWT token with JTI (JWT ID) for revocation support
     * Prompt 4: Token Revocation feature
     */
    public String createToken(String username, String role) {
        String jti = UUID.randomUUID().toString(); // Unique token ID for revocation
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        return Jwts.builder()
                .setId(jti) // Add JTI claim for revocation tracking
                .setSubject(username)
                .claim("authorities", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validate JWT token signature and expiration
     */
    public boolean validateJwt(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Extract username from JWT token
     */
    public String getUsernameFromJwt(String token) {
        return Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody().getSubject();
    }
    
    /**
     * Extract JTI (JWT ID) from token for revocation checking
     * Prompt 4: Token Revocation feature
     */
    public String getJtiFromJwt(String token) {
        return Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody().getId();
    }
    
    /**
     * Extract expiration date from token
     */
    public Date getExpirationFromJwt(String token) {
        return Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody().getExpiration();
    }
    
    /**
     * Calculate remaining expiration time in seconds
     */
    public long getRemainingExpirationSeconds(String token) {
        Date expirationDate = getExpirationFromJwt(token);
        Date now = new Date();
        if (expirationDate.before(now)) {
            return 0;
        }
        return (expirationDate.getTime() - now.getTime()) / 1000;
    }
}
