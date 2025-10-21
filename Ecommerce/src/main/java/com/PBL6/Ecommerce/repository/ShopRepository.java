package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.PBL6.Ecommerce.domain.User;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    
    // ✅ Tìm shop theo userId - SỬA s.user.id → s.owner.id
    @Query("SELECT s FROM Shop s WHERE s.owner.id = :userId")
    Optional<Shop> findByUserId(@Param("userId") Long userId);
    
    // ✅ Xóa shop theo userId - SỬA s.user.id → s.owner.id
    @Modifying
    @Transactional
    @Query("DELETE FROM Shop s WHERE s.owner.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    Optional<Shop> findByOwnerId(Long ownerId);
    Optional<Shop> findByOwner(User owner); // Thêm method này
    boolean existsByOwner(User owner); // Thêm method này
    boolean existsByName(String name);
    Optional<Shop> findByName(String name);
}

