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

    /**
     * Tạo access token với sub=username (chuẩn OAuth2/JWT)
     * userId, email, roles là các claim bổ sung
     */
    public String createToken(Long userId, String username, String email, List<String> roles) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtExpirationMs);

        JwtBuilder b = Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(signingKey(), SignatureAlgorithm.HS256);

        // THAY ĐỔI: ưu tiên username cho sub
        if (username != null) {
            b.setSubject(username);
        } else if (userId != null) {
            b.setSubject(String.valueOf(userId)); // fallback nếu không có username
        }

        // Các claim bổ sung
        if (userId != null) b.claim("userId", userId);
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

    /**
     * Lấy userId từ claim "userId" (không phải sub nữa)
     */
    public Long getUserIdFromJwt(String token) {
        Claims claims = getAllClaims(token);
        Object userIdObj = claims.get("userId");
        if (userIdObj == null) return null;
        
        // Xử lý cả Integer và Long
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        
        // Fallback: thử parse sub (cho token cũ)
        String sub = claims.getSubject();
        if (sub != null) {
            try {
                return Long.parseLong(sub);
            } catch (NumberFormatException ignored) {}
        }
        
        return null;
    }

    /**
     * Lấy username từ sub (hoặc claim "username" nếu sub là userId)
     */
    public String getUsernameFromJwt(String token) {
        Claims claims = getAllClaims(token);
        
        // Ưu tiên lấy từ sub
        String sub = claims.getSubject();
        if (sub != null && !sub.matches("\\d+")) { // nếu sub không phải số
            return sub;
        }
        
        // Fallback: lấy từ claim "username"
        Object usernameObj = claims.get("username");
        return usernameObj != null ? String.valueOf(usernameObj) : sub;
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