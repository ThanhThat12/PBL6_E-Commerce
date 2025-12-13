// ...existing code...
package com.PBL6.Ecommerce.controller.auth;

import com.PBL6.Ecommerce.service.GoogleAuthService;
import com.PBL6.Ecommerce.service.RefreshTokenService;
import com.PBL6.Ecommerce.util.TokenProvider;
import com.PBL6.Ecommerce.domain.dto.GoogleLoginDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import io.swagger.v3.oas.annotations.tags.Tag;
@Tag(name = "Google OAuth", description = "Google OAuth authentication")
@RestController
@RequestMapping("/api")
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    public GoogleAuthController(GoogleAuthService googleAuthService,
                                TokenProvider tokenProvider,
                                RefreshTokenService refreshTokenService) {
        this.googleAuthService = googleAuthService;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/authenticate/google")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> loginWithGoogle(@Valid @RequestBody GoogleLoginDTO dto) throws Exception {
    
        var user = googleAuthService.loginWithGoogle(dto);

        String accessToken = tokenProvider.createToken(user.getId(), user.getUsername(), user.getEmail(), java.util.List.of(user.getRole().name()));
        var rt = refreshTokenService.createRefreshToken(user);

        Map<String, Object> data = new HashMap<>();
        data.put("token", accessToken);
        data.put("refreshToken", rt.getToken());
        ResponseDTO<Map<String, Object>> response = new ResponseDTO<>(200, null, "Login successful", data);
        return ResponseEntity.ok(response);
    }

}
