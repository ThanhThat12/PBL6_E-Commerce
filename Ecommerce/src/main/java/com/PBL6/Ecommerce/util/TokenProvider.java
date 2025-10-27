// ...existing code...
package com.PBL6.Ecommerce.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class TokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    private Key signingKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // Create access token with sub=userId and claims
    public String createToken(Long userId, String username, String email, List<String> roles) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtExpirationMs);

        JwtBuilder b = Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(signingKey(), SignatureAlgorithm.HS256);

        if (userId != null) {
            b.setSubject(String.valueOf(userId));
        } else if (username != null) {
            b.setSubject(username);
        }

        if (username != null) b.claim("username", username);
        if (email != null) b.claim("email", email);
        if (roles != null && !roles.isEmpty()) b.claim("roles", roles);

        return b.compact();
    }

    public boolean validateJwt(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(signingKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public Claims getAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(signingKey()).build().parseClaimsJws(token).getBody();
    }

    public Long getUserIdFromJwt(String token) {
        Claims claims = getAllClaims(token);
        String sub = claims.getSubject();
        if (sub == null) return null;
        try { return Long.parseLong(sub); } catch (NumberFormatException ignored) { return null; }
    }

    public String getUsernameFromJwt(String token) {
        Claims claims = getAllClaims(token);
        Object u = claims.get("username");
        return u != null ? String.valueOf(u) : claims.getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromJwt(String token) {
        Claims claims = getAllClaims(token);
        Object r = claims.get("roles");
        if (r instanceof List) return (List<String>) r;
        if (r instanceof String) return List.of(String.valueOf(r));
        return List.of();
    }

}