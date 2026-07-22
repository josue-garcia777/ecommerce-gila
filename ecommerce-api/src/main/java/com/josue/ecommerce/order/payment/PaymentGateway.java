package com.josue.ecommerce.order.payment;

import com.josue.ecommerce.shared.ValueObjects.Money;

import java.util.UUID;

public interface PaymentGateway {

    String authorize(UUID orderId, Money amount, String idempotencyKey);
}
