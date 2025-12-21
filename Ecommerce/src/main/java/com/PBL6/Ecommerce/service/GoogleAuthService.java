// ...existing code...
package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.util.TokenProvider;
import com.PBL6.Ecommerce.domain.dto.GoogleLoginDTO;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.PBL6.Ecommerce.domain.entity.user.Role;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Value("${google.clientId}")
    private String googleClientId;

    public GoogleAuthService(UserRepository userRepository,
                             PasswordEncoder passwordEncoder,
                             TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public User loginWithGoogle(GoogleLoginDTO dto) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(dto.getIdToken());
        if (idToken == null) {
            throw new RuntimeException("Invalid Google token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = payload.get("name") != null ? (String) payload.get("name") : null;
        String googleId = payload.getSubject(); // Google's unique user id

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Google account has no email");
        }

        // 1) If there's already a user linked by this googleId -> return it
        Optional<User> byGoogle = userRepository.findOneByGoogleId(googleId);
        if (byGoogle.isPresent()) {
            return byGoogle.get();
        }

        // 2) If there's a user with the same email
        Optional<User> byEmail = userRepository.findOneByEmail(email);
        if (byEmail.isPresent()) {
            User user = byEmail.get();
            // If not yet linked to Google, link now (we verified token)
            if (user.getGoogleId() == null || user.getGoogleId().isBlank()) {
                user.setGoogleId(googleId);
                if (!user.isActivated()) user.setActivated(true);
                // Preserve existing username - only set if null
                if (user.getUsername() == null || user.getUsername().isBlank()) {
                    user.setUsername(email.split("@")[0]);
                }
                // NOTE: Existing user data (password, profile, etc.) is preserved
                // Only googleId is added to enable Google login for this account
                return userRepository.save(user);
            }

            // If linked but with different googleId -> conflict
            if (!user.getGoogleId().equals(googleId)) {
                throw new RuntimeException("Email đã được liên kết với tài khoản Google khác");
            }

            // linked and matches -> return
            return user;
        }

        // 3) No user found -> create new user linked to Google
        User user = new User();
        user.setUsername((name != null && !name.isBlank()) ? name.replaceAll("\\s+", "").toLowerCase() : email.split("@")[0]);
        user.setEmail(email);
        user.setGoogleId(googleId);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setActivated(true);
        user.setRole(Role.BUYER);
        return userRepository.save(user);
    }
}
// ...existing code...