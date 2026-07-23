package com.josue.ecommerce.cart.service.checkout;

import com.josue.ecommerce.shared.ValueObjects.Money;

import java.util.UUID;

public record CheckoutLineSnapshot(UUID productId,
                                   String sku,
                                   String productName,
                                   Money unitPrice,
                                   int quantity,
                                   Money lineTotal) {
}
