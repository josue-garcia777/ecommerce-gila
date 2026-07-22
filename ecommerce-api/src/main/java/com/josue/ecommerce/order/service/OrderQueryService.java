package com.josue.ecommerce.order.service;

import com.josue.ecommerce.order.dto.OrderResponse;
import com.josue.ecommerce.order.dto.OrderSummaryResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface OrderQueryService {
    @Transactional(readOnly = true)
    OrderResponse getOrderById(UUID orderId);

    @Transactional(readOnly = true)
    List<OrderSummaryResponse> getOrders();
}
