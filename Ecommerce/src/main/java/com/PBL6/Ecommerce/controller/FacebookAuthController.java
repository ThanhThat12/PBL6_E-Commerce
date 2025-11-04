// ...existing code...
package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.service.FacebookAuthService;
import com.PBL6.Ecommerce.service.RefreshTokenService;
import com.PBL6.Ecommerce.util.TokenProvider;
import com.PBL6.Ecommerce.domain.dto.FacebookLoginDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FacebookAuthController {

    private final FacebookAuthService facebookAuthService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    public FacebookAuthController(FacebookAuthService facebookAuthService,
                                  TokenProvider tokenProvider,
                                  RefreshTokenService refreshTokenService) {
        this.facebookAuthService = facebookAuthService;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/authenticate/facebook")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> loginWithFacebook(@RequestBody FacebookLoginDTO dto) {
        var user = facebookAuthService.loginWithFacebook(dto);

        String accessToken = tokenProvider.createToken(user.getId(), user.getUsername(), user.getEmail(), java.util.List.of(user.getRole().name()));
        var rt = refreshTokenService.createRefreshToken(user);

        Map<String, Object> data = new HashMap<>();
        data.put("token", accessToken);
        data.put("refreshToken", rt.getToken());

        ResponseDTO<Map<String, Object>> response =
                new ResponseDTO<>(200, null, "Login with Facebook successful", data);
        return ResponseEntity.ok(response);
    }
}