package com.josue.ecommerce.product.service.cmd;

import java.math.BigDecimal;

public record ProductImportCommand(
        String sku,
        String name,
        String description,
        String category,
        BigDecimal price,
        int stock,
        BigDecimal weightKg
) {
}
