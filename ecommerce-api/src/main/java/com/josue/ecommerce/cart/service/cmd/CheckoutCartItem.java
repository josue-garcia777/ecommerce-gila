package com.josue.ecommerce.cart.service.cmd;

import java.util.UUID;

public record CheckoutCartItem(UUID productId, int quantity) {
}
