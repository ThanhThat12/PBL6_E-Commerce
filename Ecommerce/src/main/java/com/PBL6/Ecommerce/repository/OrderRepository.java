package com.PBL6.Ecommerce.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.User;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find unpaid MoMo orders older than a given time (e.g., 5 minutes)
    @Query("SELECT o FROM Order o WHERE o.method = 'MOMO' AND o.paymentStatus = 'UNPAID' AND o.createdAt < :cutoff")
    List<Order> findUnpaidMomoOrdersBefore(@Param("cutoff") java.time.LocalDateTime cutoff);

    // Lấy tất cả đơn hàng theo shop
    List<Order> findByShop(Shop shop);
    
    // Lấy tất cả đơn hàng theo shop_id
    List<Order> findByShopId(Long shopId);
    
    // Lấy tất cả đơn hàng theo user (buyer)
    List<Order> findByUser(User user);
    
    // Lấy đơn hàng theo user ID
    List<Order> findByUserId(Long userId);
    
    // Custom query để lấy các trường cụ thể
    @Query("SELECT o FROM Order o WHERE o.shop.id = :shopId ORDER BY o.createdAt DESC")
    List<Order> findOrdersByShopId(@Param("shopId") Long shopId);
    
    // Tính tổng doanh thu của shop (chỉ đơn COMPLETED)
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
           "WHERE o.shop.id = :shopId AND o.status = 'COMPLETED'")
    BigDecimal calculateTotalRevenue(@Param("shopId") Long shopId);
    
    // Đếm số đơn hàng COMPLETED của shop
    @Query("SELECT COUNT(o) FROM Order o " +
           "WHERE o.shop.id = :shopId AND o.status = 'COMPLETED'")
    Long countCompletedOrders(@Param("shopId") Long shopId);
    
    // Lấy doanh thu theo tháng (chỉ đơn COMPLETED)
    @Query("SELECT YEAR(o.createdAt) as year, MONTH(o.createdAt) as month, " +
           "COALESCE(SUM(o.totalAmount), 0) as revenue, COUNT(o) as orderCount " +
           "FROM Order o " +
           "WHERE o.shop.id = :shopId AND o.status = 'COMPLETED' " +
           "GROUP BY YEAR(o.createdAt), MONTH(o.createdAt) " +
           "ORDER BY YEAR(o.createdAt), MONTH(o.createdAt)")
    List<Object[]> getMonthlyRevenue(@Param("shopId") Long shopId);
    
    // Lấy doanh thu theo tháng của một năm cụ thể
    @Query("SELECT YEAR(o.createdAt) as year, MONTH(o.createdAt) as month, " +
           "COALESCE(SUM(o.totalAmount), 0) as revenue, COUNT(o) as orderCount " +
           "FROM Order o " +
           "WHERE o.shop.id = :shopId AND o.status = 'COMPLETED' " +
           "AND YEAR(o.createdAt) = :year " +
           "GROUP BY YEAR(o.createdAt), MONTH(o.createdAt) " +
           "ORDER BY MONTH(o.createdAt)")
    List<Object[]> getMonthlyRevenueByYear(@Param("shopId") Long shopId, @Param("year") int year);
}