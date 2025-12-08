package com.PBL6.Ecommerce.repository;
import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.dto.TopBuyerDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.User;
import java.util.Optional;

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

    
    /**
     * Lấy danh sách top buyers dựa trên tổng tiền đã chi từ các đơn hàng COMPLETED
     */
    @Query("SELECT new com.PBL6.Ecommerce.domain.dto.TopBuyerDTO(" +
           "u.id, u.username, u.email, " +
           "SUM(o.totalAmount), COUNT(o.id)) " +
           "FROM Order o " +
           "JOIN o.user u " +
           "WHERE o.status = 'COMPLETED' " +
           "GROUP BY u.id, u.username, u.email " +
           "ORDER BY SUM(o.totalAmount) DESC")
    List<TopBuyerDTO> findTopBuyers();

    /**
     * Lấy danh sách top buyers với phân trang
     */
    @Query("SELECT new com.PBL6.Ecommerce.domain.dto.TopBuyerDTO(" +
           "u.id, u.username, u.email, " +
           "SUM(o.totalAmount), COUNT(o.id)) " +
           "FROM Order o " +
           "JOIN o.user u " +
           "WHERE o.status = 'COMPLETED' " +
           "GROUP BY u.id, u.username, u.email " +
           "ORDER BY SUM(o.totalAmount) DESC")
    Page<TopBuyerDTO> findTopBuyers(Pageable pageable);

    /**
     * Lấy top buyers theo shop cụ thể (cho seller)
     */
    @Query("SELECT new com.PBL6.Ecommerce.domain.dto.TopBuyerDTO(" +
           "u.id, u.username, u.email, " +
           "SUM(o.totalAmount), COUNT(o.id)) " +
           "FROM Order o " +
           "JOIN o.user u " +
           "WHERE o.status = 'COMPLETED' AND o.shop.id = :shopId " +
           "GROUP BY u.id, u.username, u.email " +
           "ORDER BY SUM(o.totalAmount) DESC")
    List<TopBuyerDTO> findTopBuyersByShop(@Param("shopId") Long shopId);

    /**
     * Lấy top buyers với giới hạn số lượng
     */
    @Query("SELECT new com.PBL6.Ecommerce.domain.dto.TopBuyerDTO(" +
           "u.id, u.username, u.email, " +
           "SUM(o.totalAmount), COUNT(o.id)) " +
           "FROM Order o " +
           "JOIN o.user u " +
           "WHERE o.status = 'COMPLETED' " +
           "GROUP BY u.id, u.username, u.email " +
           "ORDER BY SUM(o.totalAmount) DESC")
    List<TopBuyerDTO> findTopBuyersWithLimit(Pageable pageable);

    /**
     * Lấy top buyers theo shop cụ thể với giới hạn số lượng
     */
    @Query("SELECT new com.PBL6.Ecommerce.domain.dto.TopBuyerDTO(" +
           "u.id, u.username, u.email, " +
           "SUM(o.totalAmount), COUNT(o.id)) " +
           "FROM Order o " +
           "JOIN o.user u " +
           "WHERE o.status = 'COMPLETED' AND o.shop.id = :shopId " +
           "GROUP BY u.id, u.username, u.email " +
           "ORDER BY SUM(o.totalAmount) DESC")
    List<TopBuyerDTO> findTopBuyersByShopWithLimit(@Param("shopId") Long shopId, Pageable pageable);
    


    
    /**
     * Đếm số đơn hàng đã hoàn thành của user
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = 'COMPLETED'")
    long countCompletedOrdersByUserId(@Param("userId") Long userId);
    
    /**
     * Tính tổng tiền đã chi tiêu của user (chỉ đơn COMPLETED)
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM Order o WHERE o.user.id = :userId AND o.status = 'COMPLETED'")
    Double getTotalSpentByUserId(@Param("userId") Long userId);
    
    /**
     * Lấy ngày đặt hàng gần nhất của user (chỉ đơn COMPLETED)
     */
    @Query("SELECT MAX(o.createdAt) FROM Order o WHERE o.user.id = :userId AND o.status = 'COMPLETED'")
    Optional<LocalDateTime> getLastCompletedOrderDateByUserId(@Param("userId") Long userId);
    
    /**
     * Tính tổng doanh thu từ tất cả đơn hàng COMPLETED (cho Admin Dashboard Stats)
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM Order o WHERE o.status = 'COMPLETED'")
    Double getTotalRevenueFromCompletedOrders();
    
    /**
     * Đếm số đơn hàng COMPLETED của shop (cho Admin xem seller details)
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.shop.id = :shopId AND o.status = 'COMPLETED'")
    long countCompletedOrdersByShopId(@Param("shopId") Long shopId);
    
    /**
     * Tính tổng doanh thu của shop từ đơn hàng COMPLETED (cho Admin xem seller details)
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM Order o WHERE o.shop.id = :shopId AND o.status = 'COMPLETED'")
    Double getTotalRevenueByShopId(@Param("shopId") Long shopId);
//      * Tìm tất cả đơn hàng theo status
//      * Dùng để lấy danh sách SHIPPING orders cho auto-complete
//      */
    List<Order> findByStatus(Order.OrderStatus status);
    
    /**
     * Tìm đơn hàng theo status với phân trang, sắp xếp theo ngày tạo mới nhất
     * Dùng cho Admin filter orders by status
     */
    Page<Order> findByStatusOrderByCreatedAtDesc(Order.OrderStatus status, Pageable pageable);
    
//     /**
//      * Lấy ngày đặt hàng gần nhất của user (tất cả trạng thái)
//      */
//     @Query("SELECT MAX(o.createdAt) FROM Order o WHERE o.user.id = :userId")
//     Optional<LocalDateTime> getLastOrderDateByUserId(@Param("userId") Long userId);
    @Query("select o from Order o join o.orderItems oi " +
           "where o.user.id = :userId and o.status = 'COMPLETED' and oi.productId = :productId " +
           "order by o.createdAt desc")
    java.util.List<Order> findCompletedOrdersByUserAndProduct(@Param("userId") Long userId,
                                                              @Param("productId") Long productId);

    /**
     * Thống kê số đơn hàng hoàn thành theo tháng trong 12 tháng gần nhất
     */
    @Query("SELECT new com.PBL6.Ecommerce.domain.dto.MonthlyOrderStatsDTO(" +
           "YEAR(o.createdAt), MONTH(o.createdAt), COUNT(o.id)) " +
           "FROM Order o " +
           "WHERE o.shop.id = :shopId AND o.status = 'COMPLETED' " +
           "AND o.createdAt >= :startDate " +
           "GROUP BY YEAR(o.createdAt), MONTH(o.createdAt) " +
           "ORDER BY YEAR(o.createdAt), MONTH(o.createdAt)")
    List<com.PBL6.Ecommerce.domain.dto.MonthlyOrderStatsDTO> getMonthlyCompletedOrderStats(
        @Param("shopId") Long shopId, 
        @Param("startDate") LocalDateTime startDate);

    /**
     * Thống kê số đơn hàng bị hủy theo tháng trong 12 tháng gần nhất
     */
    @Query("SELECT new com.PBL6.Ecommerce.domain.dto.MonthlyOrderStatsDTO(" +
           "YEAR(o.createdAt), MONTH(o.createdAt), COUNT(o.id)) " +
           "FROM Order o " +
           "WHERE o.shop.id = :shopId AND o.status = 'CANCELLED' " +
           "AND o.createdAt >= :startDate " +
           "GROUP BY YEAR(o.createdAt), MONTH(o.createdAt) " +
           "ORDER BY YEAR(o.createdAt), MONTH(o.createdAt)")
    List<com.PBL6.Ecommerce.domain.dto.MonthlyOrderStatsDTO> getMonthlyCancelledOrderStats(
        @Param("shopId") Long shopId, 
        @Param("startDate") LocalDateTime startDate);

    // Lấy đơn hàng theo shop và status
    List<Order> findByShopIdAndStatus(Long shopId, Order.OrderStatus status);
    
    // Lấy đơn hàng theo shop, sắp xếp theo ngày tạo
    List<Order> findByShopIdOrderByCreatedAtDesc(Long shopId);

    /**
     * Tìm các đơn hàng đã thanh toán nhưng chưa chuyển tiền cho seller
     * - paymentStatus = PAID
     * - createdAt < cutoffDate (đã qua 2 phút)
     * - updatedAt = createdAt (chưa update, tức chưa chuyển tiền)
     */
    @Query("SELECT o FROM Order o " +
           "WHERE o.paymentStatus = 'PAID' " +
           "AND o.createdAt < :cutoffDate " +
           "AND (o.updatedAt IS NULL OR o.updatedAt = o.createdAt)")
    List<Order> findOrdersReadyForSellerPayout(@Param("cutoffDate") java.util.Date cutoffDate);
    // ============= ADMIN STATISTICS QUERIES =============
    
    /**
     * Đếm số đơn hàng theo status (cho admin)
     */
    Long countByStatus(Order.OrderStatus status);
    
    /**
     * Tính tổng doanh thu của tất cả đơn hàng (không tính CANCELLED)
     * Dùng cho admin dashboard
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
           "WHERE o.status != 'CANCELLED'")
    BigDecimal calculateTotalRevenue();
   
}

