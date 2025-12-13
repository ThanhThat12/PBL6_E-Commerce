package com.PBL6.Ecommerce.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.domain.Role;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.admin.ListCustomerUserDTO;
import com.PBL6.Ecommerce.domain.dto.admin.ListSellerUserDTO;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.PBL6.Ecommerce.repository.UserRepository;

@Service
public class AdminUserService {
    
    private static final Logger log = LoggerFactory.getLogger(AdminUserService.class);
    
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;

    public AdminUserService(UserRepository userRepository, OrderRepository orderRepository,
                           ShopRepository shopRepository, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.shopRepository = shopRepository;
        this.productRepository = productRepository;
    }

    /**
     * Lấy danh sách customers theo status với phân trang
     * @param status - ACTIVE (activated=true), INACTIVE (activated=false), hoặc ALL
     * @param pageable - Thông tin phân trang
     * @return Page<ListCustomerUserDTO>
     */
    @Transactional(readOnly = true)
    public Page<ListCustomerUserDTO> getCustomersByStatusWithPaging(String status, Pageable pageable) {
        log.debug("Getting customers by status '{}' with pagination: page={}, size={}", 
            status, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<User> userPage;
        
        // Query trực tiếp từ database theo status
        if (status == null || status.isEmpty() || "ALL".equalsIgnoreCase(status)) {
            userPage = userRepository.findByRole(Role.BUYER, pageable);
        } else if ("ACTIVE".equalsIgnoreCase(status)) {
            userPage = userRepository.findByRoleAndActivated(Role.BUYER, true, pageable);
        } else if ("INACTIVE".equalsIgnoreCase(status)) {
            userPage = userRepository.findByRoleAndActivated(Role.BUYER, false, pageable);
        } else {
            // Invalid status, return empty page
            userPage = userRepository.findByRole(Role.BUYER, pageable);
        }
        
        // Convert từng user thành DTO
        Page<ListCustomerUserDTO> customerDTOPage = userPage.map(user -> {
            // Lấy thông tin đơn hàng của customer
            Long userId = user.getId();
            Integer totalOrders = orderRepository.countByBuyerId(userId);
            Double totalSpent = orderRepository.getTotalSpentByBuyerId(userId);
            
            return new ListCustomerUserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.isActivated(),
                user.getCreatedAt(),
                orderRepository.getLastOrderDateByBuyerId(userId),
                totalOrders != null ? totalOrders : 0,
                totalSpent != null ? totalSpent : 0.0
            );
        });
        
        log.debug("Returning {} customers for status '{}', page {}/{}", 
            customerDTOPage.getContent().size(), status, 
            customerDTOPage.getNumber(), customerDTOPage.getTotalPages());
        
        return customerDTOPage;
    }

    /**
     * Tính tổng số customers theo status
     * Note: This method is no longer needed as Page already contains total count
     */
    private long calculateTotalElements(String status) {
        if (status == null || status.isEmpty() || "ALL".equalsIgnoreCase(status)) {
            return userRepository.countByRole(Role.BUYER);
        } else if ("ACTIVE".equalsIgnoreCase(status)) {
            return userRepository.countByRoleAndActivated(Role.BUYER, true);
        } else if ("INACTIVE".equalsIgnoreCase(status)) {
            return userRepository.countByRoleAndActivated(Role.BUYER, false);
        }
        return 0;
    }

    /**
     * Lấy danh sách sellers từ bảng shop với phân trang
     * @param pageable - Thông tin phân trang
     * @return Page<ListSellerUserDTO>
     */
    @Transactional(readOnly = true)
    public Page<ListSellerUserDTO> getSellersWithPaging(Pageable pageable) {
        log.debug("Getting sellers from shop table with pagination: page={}, size={}", 
            pageable.getPageNumber(), pageable.getPageSize());
        
        // Tạo pageable mới với sort theo ID giảm dần (mới nhất trước)
        Pageable sortedPageable = PageRequest.of(
            pageable.getPageNumber(), 
            pageable.getPageSize(), 
            Sort.by(Sort.Direction.DESC, "id")
        );
        
        // Lấy tất cả shops từ database với phân trang và sort
        Page<Shop> shopPage = shopRepository.findAll(sortedPageable);
        
        // Convert từng shop thành DTO
        Page<ListSellerUserDTO> sellerDTOPage = shopPage.map(shop -> {
            User owner = shop.getOwner();
            
            // Đếm tổng sản phẩm của shop
            int totalProducts = (int) productRepository.countByShopId(shop.getId());
            
            // Tính tổng doanh thu từ các đơn hàng COMPLETED
            Double revenue = orderRepository.getTotalRevenueByShopId(shop.getId());
            
            return new ListSellerUserDTO(
                owner.getId(),
                shop.getName(),
                owner.getPhoneNumber(),
                owner.getEmail(),
                totalProducts,
                shop.getStatus() != null ? shop.getStatus().name() : "PENDING",
                revenue != null ? revenue : 0.0
            );
        });
        
        log.debug("Returning {} sellers, page {}/{}", 
            sellerDTOPage.getContent().size(), 
            sellerDTOPage.getNumber(), 
            sellerDTOPage.getTotalPages());
        
        return sellerDTOPage;
    }
}
