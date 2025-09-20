package com.PBL6.Ecommerce.repository;
import com.PBL6.Ecommerce.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findOneByUsername(String username);
    Optional<User> findOneByEmail(String email);
    Optional<User> findOneByPhoneNumber(String phoneNumber);
    Optional<User> findOneByFacebookId(String facebookId);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
}
