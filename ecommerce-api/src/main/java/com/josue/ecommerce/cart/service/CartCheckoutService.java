package com.josue.ecommerce.cart.service;

import com.josue.ecommerce.cart.service.cmd.CheckoutCart;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface CartCheckoutService {
    @Transactional
    CheckoutCart checkoutCartForUser(UUID cartId, UUID userId);

    @Transactional
    void markCheckedOut(UUID cartId);
}
