package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.service.FacebookAuthService;
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

    public FacebookAuthController(FacebookAuthService facebookAuthService) {
        this.facebookAuthService = facebookAuthService;
    }
    
    @PostMapping("authenticate/facebook")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> loginWithFacebook(@RequestBody FacebookLoginDTO dto) {
    String token = facebookAuthService.loginWithFacebook(dto);
    Map<String, Object> data = new HashMap<>();
    data.put("token", token);
    ResponseDTO<Map<String, Object>> response =
            new ResponseDTO<>(200, null, "Login with Facebook successful", data);
    return ResponseEntity.ok(response);
}
}
