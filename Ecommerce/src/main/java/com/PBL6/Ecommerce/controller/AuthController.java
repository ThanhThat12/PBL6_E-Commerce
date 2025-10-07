package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.service.AuthService;

import com.PBL6.Ecommerce.domain.dto.LoginDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;




import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final com.PBL6.Ecommerce.repository.UserRepository userRepository;

    public AuthController(AuthService authService, com.PBL6.Ecommerce.repository.UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> login(@RequestBody LoginDTO loginDTO) {
        String token = authService.authenticate(loginDTO);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        ResponseDTO<Map<String, Object>> response = new ResponseDTO<>(200, null, "Login successful", data);
        return ResponseEntity.ok(response);
    }
}
