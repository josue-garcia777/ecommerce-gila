package com.josue.ecommerce.cart.dto;

import com.josue.ecommerce.cart.domain.CartStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID id,
        CartStatus status,
        List<CartItemResponse> items,
        CartMoneyResponse subtotal,
        long version,
        Instant createdAt,
        Instant updatedAt
) {
}
