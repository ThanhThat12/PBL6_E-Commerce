package com.PBL6.Ecommerce.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.PBL6.Ecommerce.domain.Cart;
import com.PBL6.Ecommerce.domain.User;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
