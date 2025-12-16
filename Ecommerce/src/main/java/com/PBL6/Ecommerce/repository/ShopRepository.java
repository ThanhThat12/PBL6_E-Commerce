package com.PBL6.Ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.domain.entity.shop.Shop;
import com.PBL6.Ecommerce.domain.entity.shop.Shop.ShopStatus;
import com.PBL6.Ecommerce.domain.entity.user.User;

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
    
    // Đếm số shop theo trạng thái
    long countByStatus(ShopStatus status);
    
    // Lấy danh sách shop theo trạng thái
    List<Shop> findByStatus(ShopStatus status);
    
    // ========== SELLER REGISTRATION - New queries ==========
    
    /**
     * Find pending shops (registration applications) ordered by submission date
     */
    Page<Shop> findByStatusOrderBySubmittedAtDesc(ShopStatus status, Pageable pageable);
    
    /**
     * Check if shop name exists in ACTIVE or PENDING status
     */
    @Query("SELECT COUNT(s) > 0 FROM Shop s WHERE s.name = :name AND s.status IN :statuses")
    boolean existsByNameAndStatusIn(@Param("name") String name, @Param("statuses") List<ShopStatus> statuses);
    
    /**
     * Find shop by owner and status
     */
    Optional<Shop> findByOwnerAndStatus(User owner, ShopStatus status);
    
    /**
     * Find shop by owner with any of given statuses
     */
    @Query("SELECT s FROM Shop s WHERE s.owner = :owner AND s.status IN :statuses")
    Optional<Shop> findByOwnerAndStatusIn(@Param("owner") User owner, @Param("statuses") List<ShopStatus> statuses);
    
    /**
     * Search pending applications by shop name, email, or phone
     */
    @Query("SELECT s FROM Shop s WHERE s.status = 'PENDING' AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.shopEmail) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "s.shopPhone LIKE CONCAT('%', :keyword, '%'))")
    Page<Shop> searchPendingApplications(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Check if ID card number already exists in ACTIVE or PENDING shops
     * Used to prevent duplicate CCCD registration
     */
    @Query("SELECT COUNT(s) > 0 FROM Shop s WHERE s.idCardNumber = :idCardNumber AND s.status IN :statuses")
    boolean existsByIdCardNumberAndStatusIn(@Param("idCardNumber") String idCardNumber, @Param("statuses") List<ShopStatus> statuses);
    
    /**
     * Find shop by ID card number (for checking duplicates)
     */
    Optional<Shop> findByIdCardNumber(String idCardNumber);
    
    // ========== SEARCH ==========
    
    /**
     * Search shops by name (case-insensitive, partial match)
     */
    @Query("SELECT s FROM Shop s WHERE s.status = 'ACTIVE' AND LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY s.name")
    List<Shop> findByNameContaining(@Param("query") String query, org.springframework.data.domain.Pageable pageable);
}
