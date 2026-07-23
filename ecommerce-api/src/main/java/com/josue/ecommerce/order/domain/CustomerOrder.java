package com.josue.ecommerce.order.domain;

import com.josue.ecommerce.shared.ValueObjects.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customer_orders")
@Getter
public class CustomerOrder {

    @Id
    private UUID id;

    @Column(name = "cart_id", nullable = false, unique = true)
    private UUID cartId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "total_amount", nullable = false,
                    precision = 19, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "total_currency", nullable = false,
                    length = 3, columnDefinition = "CHAR(3)"))
    })
    private Money total;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 128)
    private String idempotencyKey;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CustomerOrder() {
    }

    public CustomerOrder(UUID id, UUID cartId, UUID userId, Money total, String idempotencyKey, Instant createdAt) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 128) {
            throw new IllegalArgumentException("A valid idempotency key is required");
        }
        this.id = id;
        this.cartId = cartId;
        this.userId = userId;
        this.status = OrderStatus.PENDING_PAYMENT;
        this.total = total;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
    }

    public void confirm(String paymentReference) {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Only a pending order can be confirmed");
        }
        if (paymentReference == null || paymentReference.isBlank() || paymentReference.length() > 100) {
            throw new IllegalArgumentException("A valid payment reference is required");
        }
        this.paymentReference = paymentReference;
        this.status = OrderStatus.CONFIRMED;
    }
}
