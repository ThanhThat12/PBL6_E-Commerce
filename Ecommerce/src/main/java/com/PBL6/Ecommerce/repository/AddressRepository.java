package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.Address;
import java.util.List;
import java.util.Optional;
import com.PBL6.Ecommerce.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import com.PBL6.Ecommerce.constant.TypeAddress;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    Optional<Address> findByIdAndUserId(Long id, Long userId);
    Optional<Address> findByUserIdAndPrimaryAddressTrue(Long userId);
// Tìm tất cả địa chỉ của user theo type
    List<Address> findByUserAndTypeAddress(User user, TypeAddress typeAddress);
    // new: find first address of user by enum type (STORE / HOME / ...)
    Optional<Address> findFirstByUserIdAndTypeAddress(Long userId, TypeAddress typeAddress);
}