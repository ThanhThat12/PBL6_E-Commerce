package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.User;
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
import com.PBL6.Ecommerce.domain.Role;
import java.util.Collections;
import java.util.Optional;

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

    public String loginWithGoogle(GoogleLoginDTO dto) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(dto.getIdToken());
        if (idToken == null) {
            
    System.out.println("Invalid Google token: " + dto.getIdToken());
    throw new RuntimeException("Invalid Google token");
}

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = (String) payload.get("name");  // Thay vì "username"
        String givenName = (String) payload.get("given_name");
        String familyName = (String) payload.get("family_name");

        Optional<User> userOpt = userRepository.findOneByEmail(email);
        User user;
        if (userOpt.isEmpty()) {
            user = new User();
            user.setUsername(email.split("@")[0]);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("google_" + email)); 
            // user.setAvatar(picture);
            user.setActivated(true);
            user.setRole(Role.BUYER);
            user = userRepository.save(user);
        } else {
            user = userOpt.get();
        }

        String token = tokenProvider.createToken(user.getUsername(), user.getRole().name());

        return token;
    }
}
