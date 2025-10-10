package com.ecommerce.sportcommerce.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.ecommerce.sportcommerce.entity.User;

/**
 * Custom UserDetails implementation for Spring Security
 * Also implements OAuth2User for OAuth2 authentication
 */
@Getter
public class UserPrincipal implements UserDetails, OAuth2User {
    
    private final User user;
    private Map<String, Object> attributes;
    
    public UserPrincipal(User user) {
        this.user = user;
    }
    
    public UserPrincipal(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }
    
    /**
     * Factory method for standard authentication
     */
    public static UserPrincipal create(User user) {
        return new UserPrincipal(user);
    }
    
    /**
     * Factory method for OAuth2 authentication
     */
    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        return new UserPrincipal(user, attributes);
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }
    
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    @Override
    public String getUsername() {
        return user.getEmail(); // Use email as username
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != User.Status.SUSPENDED;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return user.getStatus() == User.Status.ACTIVE;
    }
    
    // OAuth2User implementation
    
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    @Override
    public String getName() {
        return String.valueOf(user.getId());
    }

}
