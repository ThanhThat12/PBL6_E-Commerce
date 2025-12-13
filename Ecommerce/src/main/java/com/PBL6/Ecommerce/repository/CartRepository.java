package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.entity.cart.Cart;
import com.PBL6.Ecommerce.domain.entity.user.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Long userId);
    Optional<Cart> findByUser(User user);
}