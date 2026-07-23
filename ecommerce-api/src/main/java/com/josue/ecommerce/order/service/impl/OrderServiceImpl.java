package com.josue.ecommerce.order.service.impl;

import com.josue.ecommerce.identity.CurrentUserProvider;
import com.josue.ecommerce.order.domain.CustomerOrder;
import com.josue.ecommerce.order.dto.OrderResponse;
import com.josue.ecommerce.order.dto.OrderSummaryResponse;
import com.josue.ecommerce.order.mapper.OrderMapper;
import com.josue.ecommerce.order.repository.OrderRepository;
import com.josue.ecommerce.order.repository.specification.OrderSpecifications;
import com.josue.ecommerce.order.service.OrderService;
import com.josue.ecommerce.shared.error.ApiException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.josue.ecommerce.shared.error.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.josue.ecommerce.order.repository.specification.OrderSpecifications.hasIdempotencyKey;
import static com.josue.ecommerce.order.repository.specification.OrderSpecifications.hasUser;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CurrentUserProvider currentUserProvider;
    private final OrderMapper orderMapper;

    public OrderServiceImpl(OrderRepository orderRepository, CurrentUserProvider currentUserProvider,
                            OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.currentUserProvider = currentUserProvider;
        this.orderMapper = orderMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public OrderResponse getOrderById(UUID orderId) {
        CustomerOrder order = orderRepository.findOne(
                        OrderSpecifications.hasIdAndUser(orderId, currentUserProvider.demoPrincipalUserId())
                                .and(OrderSpecifications.fetchItems()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found",
                        "No order exists with the supplied ID for the current user"));
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrderSummaryResponse> getOrders() {

        return orderRepository.findAll(
                        OrderSpecifications.forUser(currentUserProvider.demoPrincipalUserId()),
                        Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(orderMapper::toSummary)
                .toList();
    }

    @Transactional
    @Override
    public CustomerOrder savePendingOrder(CustomerOrder order) {
        return orderRepository.saveAndFlush(order);
    }

    @Transactional(readOnly = true)
    public Optional<CustomerOrder> findByIdempotencyKeyAndUser(String idempotencyKey, UUID userId) {

        return orderRepository.findOne(
                OrderSpecifications.hasUser(userId).and(hasIdempotencyKey(idempotencyKey))
                        .and(OrderSpecifications.fetchItems()));
    }

    @Transactional(readOnly = true)
    public Optional<CustomerOrder> findByCartIdAndUser(UUID cartId, UUID userId) {
        return orderRepository.findOne(
                OrderSpecifications.hasCartId(cartId).and(hasUser(userId)).and(OrderSpecifications.fetchItems()));
    }
}
