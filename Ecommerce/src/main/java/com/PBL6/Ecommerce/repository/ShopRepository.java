package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    
    // Tìm shop theo owner (seller)
    Optional<Shop> findByOwner(User owner);
    
    // Tìm shop theo owner_id
    Optional<Shop> findByOwnerId(Long ownerId);
}
