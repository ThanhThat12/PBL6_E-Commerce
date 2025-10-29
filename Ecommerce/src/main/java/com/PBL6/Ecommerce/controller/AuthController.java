package com.PBL6.Ecommerce.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.dto.LoginDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.exception.ExpiredRefreshTokenException;
import com.PBL6.Ecommerce.exception.InvalidRefreshTokenException;
import com.PBL6.Ecommerce.service.AuthService;
import com.PBL6.Ecommerce.service.RefreshTokenService;
import com.PBL6.Ecommerce.util.TokenProvider;

@RestController
@RequestMapping("/api")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthService authService;
    private final com.PBL6.Ecommerce.repository.UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final TokenProvider tokenProvider;

    public AuthController(AuthService authService, com.PBL6.Ecommerce.repository.UserRepository userRepository, RefreshTokenService refreshTokenService, TokenProvider tokenProvider) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> login(@RequestBody LoginDTO loginDTO) {
        String token = authService.authenticate(loginDTO);
        
        // Lấy user info
        com.PBL6.Ecommerce.domain.User user = userRepository.findOneByUsername(loginDTO.getUsername().toLowerCase()).orElse(null);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        
        if (user != null) {
            data.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole().name()
            ));
            // create and persist refresh token
            var rt = refreshTokenService.createRefreshToken(user);
            data.put("refreshToken", rt.getToken());
        }
        
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Login successful", data));
    }
    
    @PostMapping("/refresh-token")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidRefreshTokenException("Refresh token is required");
        }

        var stored = refreshTokenService.findByToken(refreshToken)
            .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        if (stored.isRevoked() || refreshTokenService.isExpired(stored)) {
            refreshTokenService.revokeToken(stored);
            throw new ExpiredRefreshTokenException("Refresh token expired or revoked");
        }

        var user = stored.getUser();

        // rotate: revoke/delete old and create new refresh token
        refreshTokenService.revokeToken(stored);
        var newRt = refreshTokenService.createRefreshToken(user);

        String newAccess = tokenProvider.createToken(user.getId(), user.getUsername(), user.getEmail(), java.util.List.of(user.getRole().name()));
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", newAccess);
        data.put("refreshToken", newRt.getToken());
        
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Token refreshed", data));
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseDTO<Object>> logout(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("Logout attempt without refresh token");
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "Logged out", null));
        }
        
        var opt = refreshTokenService.findByToken(refreshToken);
        if (opt.isPresent()) {
            refreshTokenService.revokeToken(opt.get());
            log.info("Refresh token revoked on logout");
        }
        
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Logged out", null));
    }
}
