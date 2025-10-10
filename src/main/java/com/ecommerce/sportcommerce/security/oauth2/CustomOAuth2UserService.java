package com.ecommerce.sportcommerce.security.oauth2;

import com.ecommerce.sportcommerce.entity.User;
import com.ecommerce.sportcommerce.exception.BadRequestException;
import com.ecommerce.sportcommerce.repository.UserRepository;
import com.ecommerce.sportcommerce.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Custom OAuth2 User Service
 * Process user info from OAuth2 providers (Google, Facebook)
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    
    private final UserRepository userRepository;
    
    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception ex) {
            logger.error("Error processing OAuth2 user: {}", ex.getMessage());
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }
    
    /**
     * Process OAuth2 user - create or update user in database
     */
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        OAuth2UserInfo oauth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                registrationId, 
                oauth2User.getAttributes()
        );
        
        if (!StringUtils.hasText(oauth2UserInfo.getEmail())) {
            throw new BadRequestException("Email không được trả về từ " + registrationId);
        }
        
        Optional<User> userOptional = userRepository.findByEmail(oauth2UserInfo.getEmail());
        User user;
        
        if (userOptional.isPresent()) {
            user = userOptional.get();
            
            // Check if user exists with different provider
            User.Provider currentProvider = User.Provider.valueOf(registrationId.toUpperCase());
            if (!user.getProvider().equals(currentProvider)) {
                throw new BadRequestException(
                    "Email này đã được đăng ký với " + user.getProvider() + 
                    ". Vui lòng sử dụng phương thức đăng nhập " + user.getProvider()
                );
            }
            
            // Update existing user
            user = updateExistingUser(user, oauth2UserInfo);
        } else {
            // Create new user
            user = registerNewUser(registrationId, oauth2UserInfo);
        }
        
        return UserPrincipal.create(user, oauth2User.getAttributes());
    }
    
    /**
     * Register new OAuth2 user
     */
    private User registerNewUser(String registrationId, OAuth2UserInfo oauth2UserInfo) {
        User user = new User();
        
        user.setEmail(oauth2UserInfo.getEmail());
        user.setProvider(User.Provider.valueOf(registrationId.toUpperCase()));
        user.setProviderId(oauth2UserInfo.getId());
        user.setEmailVerified(true); // OAuth2 providers verify email
        user.setAvatarUrl(oauth2UserInfo.getImageUrl());
        
        // Parse name into firstName and lastName
        String name = oauth2UserInfo.getName();
        if (StringUtils.hasText(name)) {
            String[] nameParts = name.split(" ", 2);
            user.setFirstName(nameParts[0]);
            if (nameParts.length > 1) {
                user.setLastName(nameParts[1]);
            }
        }
        
        // Generate username from email
        String username = oauth2UserInfo.getEmail().split("@")[0];
        user.setUsername(username);
        
        // Set default values
        user.setRole(User.Role.BUYER);
        user.setStatus(User.Status.ACTIVE);
        user.setLastLoginAt(LocalDateTime.now());
        
        logger.info("Registering new OAuth2 user with email: {}", user.getEmail());
        
        return userRepository.save(user);
    }
    
    /**
     * Update existing OAuth2 user
     */
    private User updateExistingUser(User existingUser, OAuth2UserInfo oauth2UserInfo) {
        // Update avatar if changed
        if (StringUtils.hasText(oauth2UserInfo.getImageUrl()) && 
            !oauth2UserInfo.getImageUrl().equals(existingUser.getAvatarUrl())) {
            existingUser.setAvatarUrl(oauth2UserInfo.getImageUrl());
        }
        
        // Update name if changed
        String name = oauth2UserInfo.getName();
        if (StringUtils.hasText(name)) {
            String[] nameParts = name.split(" ", 2);
            if (!nameParts[0].equals(existingUser.getFirstName())) {
                existingUser.setFirstName(nameParts[0]);
            }
            if (nameParts.length > 1 && !nameParts[1].equals(existingUser.getLastName())) {
                existingUser.setLastName(nameParts[1]);
            }
        }
        
        // Update last login
        existingUser.setLastLoginAt(LocalDateTime.now());
        
        logger.info("Updating OAuth2 user with email: {}", existingUser.getEmail());
        
        return userRepository.save(existingUser);
    }
}
