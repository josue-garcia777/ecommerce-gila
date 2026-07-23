package com.josue.ecommerce.cart.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "carts")
@Getter
public class Cart {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CartStatus status;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Cart() {
    }

    public Cart(UUID id, UUID userId, Instant now) {
        this.id = id;
        this.userId = userId;
        this.status = CartStatus.ACTIVE;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void touch(Instant now) {
        requireActive();
        updatedAt = now;
    }

    public void checkOut(Instant now) {
        requireActive();
        status = CartStatus.CHECKED_OUT;
        updatedAt = now;
    }

    private void requireActive() {
        if (status != CartStatus.ACTIVE) {
            throw new IllegalStateException("Cart is not active");
        }
    }

}
