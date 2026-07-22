package com.josue.ecommerce.product.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        String category,
        MoneyResponse price,
        int stock,
        BigDecimal weightKg,
        String imageUrl,
        boolean active,
        long version,
        Instant createdAt,
        Instant updatedAt
) {
}
