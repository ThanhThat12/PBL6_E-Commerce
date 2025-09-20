package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.service.GoogleAuthService;
import com.PBL6.Ecommerce.domain.dto.GoogleLoginDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;

    public GoogleAuthController(GoogleAuthService googleAuthService) {
        this.googleAuthService = googleAuthService;
    }

    @PostMapping("/authenticate/google")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> loginWithGoogle(@Valid @RequestBody GoogleLoginDTO dto) throws Exception {
    String token = googleAuthService.loginWithGoogle(dto);
    Map<String, Object> data = new HashMap<>();
    data.put("token", token);
    ResponseDTO<Map<String, Object>> response = new ResponseDTO<>(200, null, "Login successful", data);
    return ResponseEntity.ok(response);
    }
  
}