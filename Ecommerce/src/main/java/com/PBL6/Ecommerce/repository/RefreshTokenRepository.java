package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.RefreshToken;
import com.PBL6.Ecommerce.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    int deleteByUser(User user);
}