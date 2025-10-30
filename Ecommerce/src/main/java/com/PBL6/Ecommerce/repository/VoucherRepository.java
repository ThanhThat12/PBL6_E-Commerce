package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.Vouchers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Vouchers, Long> {
    
    Optional<Vouchers> findByCode(String code);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Vouchers v WHERE v.code = :code")
    Optional<Vouchers> findByCodeWithLock(@Param("code") String code);
}
