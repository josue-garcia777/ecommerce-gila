package com.josue.ecommerce.order.payment;

import com.josue.ecommerce.shared.ValueObjects.Money;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FakePaymentGateway implements PaymentGateway {

    private final String failureIdempotencyKey;

    public FakePaymentGateway(@Value("${app.payment.fake.fail-key:}") String failureIdempotencyKey) {
        this.failureIdempotencyKey = failureIdempotencyKey;
    }

    @Override
    public String authorize(UUID orderId, Money amount, String idempotencyKey) {

        if (!failureIdempotencyKey.isBlank() && failureIdempotencyKey.equals(idempotencyKey)) {
            throw new PaymentGatewayException("Configured fake payment failure");
        }

        UUID authorizationId = UUID.nameUUIDFromBytes(idempotencyKey.getBytes(StandardCharsets.UTF_8));
        return "FAKE-" + authorizationId.toString().toUpperCase(java.util.Locale.ROOT);
    }
}
