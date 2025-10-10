package com.ecommerce.sportcommerce.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ecommerce.sportcommerce.entity.User;
import com.ecommerce.sportcommerce.exception.ResourceNotFoundException;
import com.ecommerce.sportcommerce.repository.UserRepository;
import com.ecommerce.sportcommerce.security.UserPrincipal;

/**
 * Custom UserDetailsService for loading user from database
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndProvider(email, User.Provider.LOCAL)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        return new UserPrincipal(user);
    }
    
    /**
     * Load user by ID
     */
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        return new UserPrincipal(user);
    }
}
