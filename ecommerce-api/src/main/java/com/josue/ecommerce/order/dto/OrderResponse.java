package com.josue.ecommerce.order.dto;

import com.josue.ecommerce.order.domain.OrderStatus;
import com.josue.ecommerce.shared.dto.AddressResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID cartId,
        OrderStatus status,
        OrderMoneyResponse total,
        String paymentReference,
        Instant createdAt,
        AddressResponse address,
        List<OrderItemResponse> items
) {
}
