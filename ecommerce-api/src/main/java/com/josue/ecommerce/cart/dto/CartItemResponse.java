package com.josue.ecommerce.cart.dto;

import java.util.UUID;

public record CartItemResponse(
        UUID productId,
        String sku,
        String productName,
        CartMoneyResponse unitPrice,
        int quantity,
        CartMoneyResponse lineTotal,
        int stock,
        boolean available,
        String imageUrl
) {
}
