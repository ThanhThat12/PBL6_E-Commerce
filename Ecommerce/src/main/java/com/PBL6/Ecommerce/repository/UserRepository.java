package com.PBL6.Ecommerce.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.PBL6.Ecommerce.domain.Role;
import com.PBL6.Ecommerce.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findOneByUsername(String username);
    Optional<User> findOneByEmail(String email);
    Optional<User> findOneByPhoneNumber(String phoneNumber);
    Optional<User> findOneByFacebookId(String facebookId);
    Optional<User> findOneByGoogleId(String googleId);
    
    // Thêm method findByEmail (không có One) để tương thích với ProductService
    Optional<User> findByEmail(String email);
    
    // Thêm method findByUsername để tương thích
    Optional<User> findByUsername(String username);
    
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    
    // Custom query để lấy tất cả users
    @Query("SELECT u FROM User u")
    List<User> getAllUsers();
    // Thêm method tìm user theo role
    List<User> findByRole(Role role);
    
    // Thêm method tìm user theo role với phân trang
    Page<User> findByRole(Role role, Pageable pageable);
    
    // Thêm method đếm số lượng user theo role
    long countByRole(Role role);
    
    // Đếm số lượng user theo role và activated status
    long countByRoleAndActivated(Role role, boolean activated);
    
    // Đếm số lượng user theo role và created after date
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.createdAt >= :startDate")
    long countByRoleAndCreatedAtAfter(@Param("role") Role role, @Param("startDate") LocalDateTime startDate);
    
    // Method để kiểm tra phone number đã được sử dụng bởi seller chưa
    List<User> findByPhoneNumberAndRole(String phoneNumber, Role role);

    
}
