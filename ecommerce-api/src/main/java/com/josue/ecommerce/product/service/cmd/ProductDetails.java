package com.josue.ecommerce.product.service.cmd;

import com.josue.ecommerce.shared.ValueObjects.Money;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductDetails(
        UUID id,
        String sku,
        String name,
        String description,
        String category,
        Money price,
        int stock,
        BigDecimal weightKg,
        String imageUrl,
        boolean active
) {
}
