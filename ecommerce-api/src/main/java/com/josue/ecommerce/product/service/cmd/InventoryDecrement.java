package com.josue.ecommerce.product.service.cmd;

import java.util.UUID;

public record InventoryDecrement(
        UUID productId,
        int quantity
) {
    public InventoryDecrement {
        if (productId == null) {
            throw new IllegalArgumentException(
                    "Product ID is required"
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }
    }
}