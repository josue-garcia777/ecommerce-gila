package com.josue.ecommerce.cart.service.cmd;

import com.josue.ecommerce.cart.domain.CartStatus;

import java.util.List;
import java.util.UUID;

public record CheckoutCart(UUID id, UUID userId, CartStatus status, List<CheckoutCartItem> items) {
}
