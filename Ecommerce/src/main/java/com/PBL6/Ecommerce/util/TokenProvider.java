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
        return Jwts.builder()
                .setSubject(username)
                .claim("authorities", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateJwt(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String getUsernameFromJwt(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }
}
