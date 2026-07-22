package com.josue.ecommerce.cart.service;

import com.josue.ecommerce.cart.dto.CartResponse;

import java.util.UUID;

public interface CartService {
    CartResponse createOrGetActive();
    CartResponse getCart(UUID cartId);
    CartResponse setQuantity(UUID cartId, UUID productId, int quantity);
    CartResponse removeProductFromCart(UUID cartId, UUID productId);
}
