package com.PBL6.Ecommerce.service;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.PBL6.Ecommerce.domain.Role;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.GoogleLoginDTO;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.util.TokenProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

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
    
    /**
     * Extract email from Google ID token
     * @param idTokenString Google ID token string
     * @return email address from token
     */
    public String getEmailFromToken(String idTokenString) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) {
            throw new RuntimeException("Invalid Google token");
        }

        return idToken.getPayload().getEmail();
    }
}
