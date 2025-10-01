package com.PBL6.Ecommerce.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

import com.PBL6.Ecommerce.domain.dto.LoginDTO;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.util.TokenProvider;
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public String authenticate(LoginDTO dto) {
        Optional<User> userOpt = userRepository.findOneByUsername(dto.getUsername().toLowerCase());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid username or password");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        if (!user.isActivated()) {
            throw new RuntimeException("User not activated");
        }

        String token = tokenProvider.createToken(user.getUsername(), user.getRole().name());

         return token;
    }
}
