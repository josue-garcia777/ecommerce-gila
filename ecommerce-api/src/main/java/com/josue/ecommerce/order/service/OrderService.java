package com.josue.ecommerce.order.service;

import com.josue.ecommerce.order.domain.CustomerOrder;
import com.josue.ecommerce.order.dto.OrderResponse;
import com.josue.ecommerce.order.dto.OrderSummaryResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderService {
    @Transactional(readOnly = true)
    OrderResponse getOrderById(UUID orderId);

    @Transactional(readOnly = true)
    List<OrderSummaryResponse> getOrders();

    @Transactional
    CustomerOrder savePendingOrder(CustomerOrder order);

    @Transactional(readOnly = true)
    Optional<CustomerOrder> findByIdempotencyKeyAndUser(String idempotencyKey, UUID userId);

    Optional<CustomerOrder> findByCartIdAndUser(UUID cartId, UUID userId);
}
