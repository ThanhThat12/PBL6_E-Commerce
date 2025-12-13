package com.PBL6.Ecommerce.repository;
import com.PBL6.Ecommerce.domain.entity.order.Order;
import com.PBL6.Ecommerce.domain.entity.shop.Shop;
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


import com.PBL6.Ecommerce.domain.entity.order.Order;
import com.PBL6.Ecommerce.domain.entity.shop.Shop;
import com.PBL6.Ecommerce.domain.entity.user.User;
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
     * - Chưa có transaction PAYMENT_TO_SELLER cho seller này
     */
    @Query("SELECT o FROM Order o " +
           "WHERE o.paymentStatus = 'PAID' " +
           "AND o.createdAt < :cutoffDate " +
           "AND NOT EXISTS (" +
           "  SELECT 1 FROM WalletTransaction wt " +
           "  WHERE wt.relatedOrder = o " +
           "  AND wt.type = 'PAYMENT_TO_SELLER' " +
           "  AND wt.wallet.user.id = o.shop.owner.id" +
           ")")
    List<Order> findOrdersReadyForSellerPayout(@Param("cutoffDate") java.util.Date cutoffDate);
    // ============= ADMIN STATISTICS QUERIES =============
    
    /**
     * Đếm số đơn hàng theo status (cho admin)
     */
    Long countByStatus(Order.OrderStatus status);
    
    /**
     * Tính tổng doanh thu của tất cả đơn hàng COMPLETED
     * Dùng cho admin dashboard
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
           "WHERE o.status = 'COMPLETED'")
    BigDecimal calculateTotalRevenue();
    
    // ============= ADMIN DASHBOARD QUERIES =============
    
    /**
     * Tính tổng doanh thu theo khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
           "WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueByDateRange(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    /**
     * Đếm số đơn hàng theo khoảng thời gian
     */
    Long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Top selling products cho admin dashboard
     */
    @Query("SELECT p.id, p.name, c.name, p.mainImage, SUM(oi.quantity), SUM(oi.price * oi.quantity), p.isActive " +
           "FROM OrderItem oi " +
           "JOIN oi.variant pv " +
           "JOIN pv.product p " +
           "LEFT JOIN p.category c " +
           "JOIN oi.order o " +
           "WHERE o.status = 'COMPLETED' " +
           "GROUP BY p.id, p.name, c.name, p.mainImage, p.isActive " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingProducts(Pageable pageable);
    
    /**
     * Recent orders cho admin dashboard
     */
    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(Pageable pageable);
   
    // ============================================
    // ADMIN DASHBOARD - Revenue Chart Methods
    // ============================================
    
    /**
     * ADMIN - Tính tổng doanh thu từ đơn COMPLETED theo khoảng thời gian
     * Dùng cho biểu đồ doanh thu admin dashboard
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
           "WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateCompletedRevenueByDateRange(@Param("startDate") LocalDateTime startDate, 
                                                    @Param("endDate") LocalDateTime endDate);
    
    /**
     * ADMIN - Đếm số đơn hàng COMPLETED theo khoảng thời gian
     * Dùng cho biểu đồ doanh thu admin dashboard
     */
    @Query("SELECT COUNT(o) FROM Order o " +
           "WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :startDate AND :endDate")
    Long countCompletedOrdersByDateRange(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
    
    // ============================================
    // ADMIN - Customer Statistics Methods
    // ============================================
    
    /**
     * Đếm tổng số đơn hàng của buyer (tất cả status)
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :buyerId")
    Integer countByBuyerId(@Param("buyerId") Long buyerId);
    
    /**
     * Tính tổng tiền đã chi của buyer từ các đơn hàng COMPLETED
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM Order o " +
           "WHERE o.user.id = :buyerId AND o.status = 'COMPLETED'")
    Double getTotalSpentByBuyerId(@Param("buyerId") Long buyerId);
    
    /**
     * Lấy ngày đặt hàng gần nhất của buyer (tất cả status)
     */
    @Query("SELECT MAX(o.createdAt) FROM Order o WHERE o.user.id = :buyerId")
    LocalDateTime getLastOrderDateByBuyerId(@Param("buyerId") Long buyerId);
}