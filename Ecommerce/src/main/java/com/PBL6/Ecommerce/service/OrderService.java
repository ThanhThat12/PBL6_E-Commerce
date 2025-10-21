package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.dto.OrderDTO;
import java.util.List;

public interface OrderService {
    List<OrderDTO> getAllOrders();
    OrderDTO getOrderById(Long id);
    OrderDTO updateOrderStatus(Long id, String status);
    OrderDTO refundOrder(Long id);
}
