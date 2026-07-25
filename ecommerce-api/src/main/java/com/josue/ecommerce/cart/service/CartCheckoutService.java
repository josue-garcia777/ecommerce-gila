package com.josue.ecommerce.cart.service;

import com.josue.ecommerce.cart.domain.Cart;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

public interface CartCheckoutService {
    @Transactional
    Cart checkoutCartForUser(UUID cartId, UUID userId);

    void beginCheckout(Cart cart, Instant now);

}
