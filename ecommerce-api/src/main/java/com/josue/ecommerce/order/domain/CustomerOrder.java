package com.josue.ecommerce.order.domain;

import com.josue.ecommerce.shared.ValueObjects.Money;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customer_orders")
@Getter
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @OrderBy("productName ASC")
    private List<OrderItem> items = new ArrayList<>();

    protected CustomerOrder() {
    }

    public CustomerOrder(UUID cartId, UUID userId, Money total, String idempotencyKey, Instant createdAt) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 128) {
            throw new IllegalArgumentException("A valid idempotency key is required");
        }
        this.cartId = cartId;
        this.userId = userId;
        this.status = OrderStatus.PENDING_PAYMENT;
        this.total = total;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(UUID productId, String sku, String productName, Money unitPrice,
                        int quantity, Money lineTotal) {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Items can only be added to a pending order");
        }
        OrderItem item = new OrderItem(this, productId, sku, productName, unitPrice, quantity, lineTotal);
        items.add(item);
    }

    public void confirm(String paymentReference) {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Only a pending order can be confirmed");
        }
        if (paymentReference == null || paymentReference.isBlank()) {
            throw new IllegalArgumentException("A valid payment reference is required");
        }
        this.paymentReference = paymentReference;
        this.status = OrderStatus.CONFIRMED;
    }
}
