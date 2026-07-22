package com.josue.ecommerce.order.dto;

import java.util.UUID;

public record OrderItemResponse(
        UUID productId,
        String sku,
        String productName,
        OrderMoneyResponse unitPrice,
        int quantity,
        OrderMoneyResponse lineTotal
) {
}
