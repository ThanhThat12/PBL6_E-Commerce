// ...existing code...
package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.domain.dto.FacebookLoginDTO;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.domain.entity.user.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class FacebookAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public FacebookAuthService(UserRepository userRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Trả về User đã tồn tại hoặc mới tạo (không trả token)
    public User loginWithFacebook(FacebookLoginDTO dto) {
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
                user.setUsername(name.replaceAll("\\s+", "").toLowerCase() + facebookId.substring(0, Math.min(5, facebookId.length())));
                user.setEmail(email);
                user.setFacebookId(facebookId);
                // mật khẩu ngẫu nhiên vì đăng nhập bằng FB
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
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

            return user;
        } catch (Exception e) {
            throw new RuntimeException("Xác thực Facebook thất bại: " + e.getMessage());
        }
    }
}