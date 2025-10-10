//package com.ecommerce.sportcommerce.service;
//
//import com.ecommerce.sportcommerce.dto.UserDto;
//import com.ecommerce.sportcommerce.entity.User;
//import com.ecommerce.sportcommerce.enums.*;
//import com.ecommerce.sportcommerce.exception.BadRequestException;
//import com.ecommerce.sportcommerce.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class UserService {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Transactional
//    public User createUser(String email, String password, Role role, Map<String, Object> additionalInfo) {
//        if (userRepository.existsByEmail(email)) {
//            throw new BadRequestException("Email already registered");
//        }
//
//        User.UserBuilder userBuilder = User.builder()
//            .email(email)
//            .password(passwordEncoder.encode(password))
//            .role(role)
//            .provider(Provider.LOCAL)
//            .emailVerificationStatus(EmailVerificationStatus.VERIFIED)
//            .status(Status.ACTIVE);
//
//        // Add SELLER specific fields
//        if (role == Role.SELLER && additionalInfo != null) {
//            userBuilder
//                .firstName((String) additionalInfo.get("firstName"))
//                .lastName((String) additionalInfo.get("lastName"))
//                .phone((String) additionalInfo.get("phone"))
//                .shopName((String) additionalInfo.get("shopName"))
//                .shopAddress((String) additionalInfo.get("shopAddress"))
//                .taxId((String) additionalInfo.get("taxId"));
//        }
//
//        User user = userBuilder.build();
//        User savedUser = userRepository.save(user);
//
//        log.info("User created successfully: {}", email);
//        return savedUser;
//    }
//
//    public UserDto convertToDto(User user) {
//        return new UserDto(
//            user.getId(),
//            user.getEmail(),
//            user.getFirstName(),
//            user.getLastName(),
//            user.getRole().name(),
//            user.getPhone(),
//            user.getShopName(),
//            user.getShopAddress(),
//            user.getTaxId(),
//            user.getEmailVerificationStatus().name(),
//            user.getProvider().name()
//        );
//    }
//}
