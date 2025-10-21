package com.PBL6.Ecommerce.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.PBL6.Ecommerce.domain.Role;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.FacebookLoginDTO;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.util.TokenProvider;

@Service
public class FacebookAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public FacebookAuthService(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public String loginWithFacebook(FacebookLoginDTO dto) {
    if (dto.getAccessToken() == null || dto.getAccessToken().isEmpty()) {
        throw new IllegalArgumentException("Access token là bắt buộc");
    }

    try {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://graph.facebook.com/v18.0/me?fields=id,name,email,picture&access_token=" + dto.getAccessToken();

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> fbUser = response.getBody();

        if (fbUser == null || !fbUser.containsKey("id")) {
            throw new RuntimeException("Phản hồi Facebook không hợp lệ");
        }

        String email = fbUser.containsKey("email") ? (String) fbUser.get("email") : null;
        String facebookId = (String) fbUser.get("id");
        String name = (String) fbUser.get("name");

        // Tìm user theo Facebook ID trước
        Optional<User> userOpt = userRepository.findOneByFacebookId(facebookId);
        
        if (userOpt.isEmpty() && email != null) {
            // Nếu không tìm thấy bằng Facebook ID, thử tìm bằng email
            userOpt = userRepository.findOneByEmail(email);
        }

        User user;
        if (userOpt.isEmpty()) {
            user = new User();
            user.setUsername(name.replaceAll("\\s+", "").toLowerCase() + facebookId.substring(0, 5));
            user.setEmail(email);
            user.setFacebookId(facebookId); // Thêm trường này vào User entity
            user.setPassword(passwordEncoder.encode("facebook_" + facebookId));
            user.setActivated(true);
            user.setRole(Role.BUYER);
            user = userRepository.save(user);
        } else {
            user = userOpt.get();
            // Cập nhật Facebook ID nếu chưa có
            if (user.getFacebookId() == null) {
                user.setFacebookId(facebookId);
                user = userRepository.save(user);
            }
        }

        return tokenProvider.createToken(user.getUsername(), user.getRole().name());
    } catch (Exception e) {
        throw new RuntimeException("Xác thực Facebook thất bại: " + e.getMessage());
    }
}

/**
 * Login with Facebook and return both access token and user object
 * Used by controller to create refresh token
 */
public Map<String, Object> loginWithFacebookAndGetUser(FacebookLoginDTO dto) {
    if (dto.getAccessToken() == null || dto.getAccessToken().isEmpty()) {
        throw new IllegalArgumentException("Access token là bắt buộc");
    }

    try {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://graph.facebook.com/v18.0/me?fields=id,name,email,picture&access_token=" + dto.getAccessToken();

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> fbUser = response.getBody();

        if (fbUser == null || !fbUser.containsKey("id")) {
            throw new RuntimeException("Phản hồi Facebook không hợp lệ");
        }

        String email = fbUser.containsKey("email") ? (String) fbUser.get("email") : null;
        String facebookId = (String) fbUser.get("id");
        String name = (String) fbUser.get("name");

        // Tìm user theo Facebook ID trước
        Optional<User> userOpt = userRepository.findOneByFacebookId(facebookId);
        
        if (userOpt.isEmpty() && email != null) {
            // Nếu không tìm thấy bằng Facebook ID, thử tìm bằng email
            userOpt = userRepository.findOneByEmail(email);
        }

        User user;
        if (userOpt.isEmpty()) {
            user = new User();
            user.setUsername(name.replaceAll("\\s+", "").toLowerCase() + facebookId.substring(0, 5));
            user.setEmail(email);
            user.setFacebookId(facebookId);
            user.setPassword(passwordEncoder.encode("facebook_" + facebookId));
            user.setActivated(true);
            user.setRole(Role.BUYER);
            user = userRepository.save(user);
        } else {
            user = userOpt.get();
            // Cập nhật Facebook ID nếu chưa có
            if (user.getFacebookId() == null) {
                user.setFacebookId(facebookId);
                user = userRepository.save(user);
            }
        }

        String accessToken = tokenProvider.createToken(user.getUsername(), user.getRole().name());
        
        // Return both token and user
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("user", user);
        return result;
        
    } catch (Exception e) {
        throw new RuntimeException("Xác thực Facebook thất bại: " + e.getMessage());
    }
}
}