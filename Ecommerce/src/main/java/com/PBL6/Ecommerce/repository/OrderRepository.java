package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByShopId(Long shopId);
    List<Order> findByUserId(Long userId);
}
