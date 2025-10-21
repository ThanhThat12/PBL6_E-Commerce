package com.PBL6.Ecommerce.repository;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findOneByUsername(String username);
    Optional<User> findOneByEmail(String email);
    Optional<User> findOneByPhoneNumber(String phoneNumber);
    Optional<User> findOneByFacebookId(String facebookId);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    
    // Thêm method tìm user theo role
    List<User> findByRole(Role role);
    
    // Thêm method đếm số lượng user theo role
    long countByRole(Role role);
}
