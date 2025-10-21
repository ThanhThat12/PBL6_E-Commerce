package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByOwnerId(Long ownerId);
    Optional<Shop> findByOwner(User owner); // Thêm method này
    boolean existsByOwner(User owner); // Thêm method này
    boolean existsByName(String name);
    Optional<Shop> findByName(String name);
}