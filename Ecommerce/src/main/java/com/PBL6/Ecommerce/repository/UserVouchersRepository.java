package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.User_Vouchers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserVouchersRepository extends JpaRepository<User_Vouchers, Long> {
    @Modifying
    @Query("DELETE FROM User_Vouchers uv WHERE uv.order.id = :orderId")
    void deleteByOrderId(@Param("orderId") Long orderId);
}
