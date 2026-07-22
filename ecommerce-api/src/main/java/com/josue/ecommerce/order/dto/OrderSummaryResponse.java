package com.josue.ecommerce.order.dto;

import com.josue.ecommerce.order.domain.OrderStatus;
import java.time.Instant;
import java.util.UUID;

public record OrderSummaryResponse(
        UUID id,
        UUID cartId,
        OrderStatus status,
        OrderMoneyResponse total,
        Instant createdAt
) {
}
