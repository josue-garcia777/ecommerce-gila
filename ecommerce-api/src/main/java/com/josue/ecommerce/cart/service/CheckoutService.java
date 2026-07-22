package com.josue.ecommerce.cart.service;

import com.josue.ecommerce.order.dto.OrderResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface CheckoutService {
    @Transactional
    OrderResponse checkout(UUID cartId, String idempotencyKey);
}
