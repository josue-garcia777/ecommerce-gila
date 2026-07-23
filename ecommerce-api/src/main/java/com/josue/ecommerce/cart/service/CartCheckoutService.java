package com.josue.ecommerce.cart.service;

import com.josue.ecommerce.cart.domain.Cart;
import com.josue.ecommerce.cart.service.cmd.CheckoutCart;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

public interface CartCheckoutService {
    @Transactional
    Cart checkoutCartForUser(UUID cartId, UUID userId);

    void claimForCheckout(Cart cart, Instant now);

}
