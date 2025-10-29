package com.PBL6.Ecommerce.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class TokenProvider {

    private final String jwtSecret = "my-secret-key-which-should-be-long"; // đọc từ config
    private final long jwtExpirationMs = 86400000;
 
    private final Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

    public String createToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        System.out.println("🔍 DEBUG - TokenProvider creating token:");
        System.out.println("🔍 DEBUG - Username: " + username);
        System.out.println("🔍 DEBUG - Role: " + role);
        System.out.println("🔍 DEBUG - Issued at: " + now + " (" + now.getTime() + ")");
        System.out.println("🔍 DEBUG - Expires at: " + expiryDate + " (" + expiryDate.getTime() + ")");
        
        return Jwts.builder()
                .setSubject(username)
                .claim("authorities", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateJwt(String authToken) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken).getBody();
            Date expiration = claims.getExpiration();
            Date now = new Date();
            
            System.out.println("🔍 DEBUG - Token validation:");
            System.out.println("🔍 DEBUG - Current time: " + now + " (" + now.getTime() + ")");
            System.out.println("🔍 DEBUG - Token expires: " + expiration + " (" + expiration.getTime() + ")");
            System.out.println("🔍 DEBUG - Is expired: " + expiration.before(now));
            
            return true;
        } catch (JwtException e) {
            System.err.println("❌ DEBUG - Token validation failed: " + e.getMessage());
            return false;
        }
    }

    // 🆕 Alias method for JwtAuthenticationFilter
    public boolean validateToken(String authToken) {
        return validateJwt(authToken);
    }

    public String getUsernameFromJwt(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    // 🆕 Alias method for JwtAuthenticationFilter
    public String getUsernameFromToken(String token) {
        return getUsernameFromJwt(token);
    }

    // 🆕 Get role from token
    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.get("authorities", String.class);
    }
}
