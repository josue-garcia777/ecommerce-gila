package com.josue.ecommerce.order.service.impl;

import com.josue.ecommerce.identity.CurrentUserProvider;
import com.josue.ecommerce.order.domain.CustomerOrder;
import com.josue.ecommerce.order.dto.OrderResponse;
import com.josue.ecommerce.order.dto.OrderSummaryResponse;
import com.josue.ecommerce.order.mapper.OrderMapper;
import com.josue.ecommerce.order.repository.OrderItemRepository;
import com.josue.ecommerce.order.repository.OrderRepository;
import com.josue.ecommerce.order.service.OrderQueryService;
import com.josue.ecommerce.shared.error.ApiException;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CurrentUserProvider currentUserProvider;
    private final OrderMapper orderMapper;

    public OrderQueryServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                                 CurrentUserProvider currentUserProvider, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.currentUserProvider = currentUserProvider;
        this.orderMapper = orderMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public OrderResponse getOrderById(UUID orderId) {
        CustomerOrder order = orderRepository.findByIdAndUserId(orderId, currentUserProvider.demoPrincipalUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found",
                        "No order exists with the supplied ID for the current user"));
        return orderMapper.toResponse(order,
                orderItemRepository.findAllByOrderIdOrderByProductName(orderId));
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrderSummaryResponse> getOrders() {

        return orderRepository.findAllByUserIdOrderByCreatedAtDesc(currentUserProvider.demoPrincipalUserId()).stream()
                .map(orderMapper::toSummary)
                .toList();
    }
}
