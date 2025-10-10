package com.ecommerce.sportcommerce.repository;

import com.ecommerce.sportcommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by email and provider
     */
    Optional<User> findByEmailAndProvider(String email, User.Provider provider);
    
    /**
     * Find user by email (for LOCAL provider by default)
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.provider = 'LOCAL'")
    Optional<User> findByEmail(@Param("email") String email);
    
    /**
     * Check if email exists with given provider
     */
    boolean existsByEmailAndProvider(String email, User.Provider provider);
    
    /**
     * Check if email exists (for LOCAL provider)
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.provider = 'LOCAL'")
    boolean existsByEmail(@Param("email") String email);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if phone exists
     */
    boolean existsByPhone(String phone);
    
    /**
     * Find by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find by provider and provider ID (for OAuth)
     */
    Optional<User> findByProviderAndProviderId(User.Provider provider, String providerId);
}
