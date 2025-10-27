// package com.PBL6.Ecommerce.util;

// import io.jsonwebtoken.*;
// import org.springframework.stereotype.Component;
// import java.util.Date;
// import java.util.HashMap;
// import java.util.Map;

// @Component
// public class JwtUtil {
//     private final String SECRET_KEY = "your_secret_key";
//     private final long EXPIRATION = 86400000; // 1 ngày

//     public String generateToken(Long userId, String username, String email, String role) {
//         Map<String, Object> claims = new HashMap<>();
//         claims.put("userId", userId);
//         claims.put("username", username);
//         claims.put("email", email);
//         claims.put("role", role);

//         return Jwts.builder()
//                 .setClaims(claims)
//                 .setSubject(username)
//                 .setIssuedAt(new Date())
//                 .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
//                 .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
//                 .compact();
//     }

//     public Claims extractClaims(String token) {
//         return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
//     }
// }
