package com.PBL6.Ecommerce.service.impl;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.OrderItem;
import com.PBL6.Ecommerce.domain.dto.OrderDTO;
import com.PBL6.Ecommerce.domain.dto.OrderItemDTO;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public OrderDTO getOrderById(Long id) {
        return orderRepository.findById(id).map(this::toDTO).orElse(null);
    }

    @Override
    public OrderDTO updateOrderStatus(Long id, String status) {
        Optional<Order> opt = orderRepository.findById(id);
        if (!opt.isPresent()) return null;
        Order order = opt.get();
        try {
            order.setStatus(Order.OrderStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            // ignore invalid
        }
        Order saved = orderRepository.save(order);
        return toDTO(saved);
    }

    @Override
    public OrderDTO refundOrder(Long id) {
        Optional<Order> opt = orderRepository.findById(id);
        if (!opt.isPresent()) return null;
        Order order = opt.get();
        // business logic: set status to CANCELLED (or custom logic)
        order.setStatus(Order.OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        return toDTO(saved);
    }

    private OrderDTO toDTO(Order order) {
        if (order == null) return null;
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getId());
            dto.setUserName(order.getUser().getUsername());
        }
        if (order.getShop() != null) {
            dto.setShopId(order.getShop().getId());
            dto.setShopName(order.getShop().getName());
        }
        dto.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        dto.setTotalAmount(order.getTotalAmount());
        dto.setMethod(order.getMethod());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        // Map orderItems
        if (order.getOrderItems() != null) {
            List<OrderItemDTO> itemDTOs = order.getOrderItems().stream().map(item -> {
                OrderItemDTO itemDTO = new OrderItemDTO();
                itemDTO.setId(item.getId());
                if (item.getProduct() != null) {
                    itemDTO.setProductId(item.getProduct().getId());
                    itemDTO.setProductName(item.getProduct().getName());
                }
                itemDTO.setQuantity(item.getQuantity());
                itemDTO.setPrice(item.getPrice());
                return itemDTO;
            }).collect(java.util.stream.Collectors.toList());
            dto.setOrderItems(itemDTOs);
        }
        return dto;
    }
}
