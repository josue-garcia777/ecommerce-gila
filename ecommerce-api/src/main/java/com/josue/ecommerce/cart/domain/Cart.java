package com.josue.ecommerce.cart.domain;

import com.josue.ecommerce.identity.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "carts")
@Getter
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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

    @OneToMany(mappedBy = "cart", fetch = FetchType.LAZY)
    private List<CartItem> items = new ArrayList<>();

    protected Cart() {
    }

    public Cart(User user, Instant now) {
        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }
        this.user = user;
        this.status = CartStatus.ACTIVE;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public UUID getUserId() {
        return user.getId();
    }

    public CartItem setProductQuantity(UUID productId, int quantity) {
        requireActive();
        Optional<CartItem> existingItem = items.stream()
                .filter(item -> item.hasProduct(productId))
                .findFirst();
        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(quantity);
            return existingItem.get();
        }
        return addProduct(productId, quantity);
    }

    public Optional<CartItem> removeProduct(UUID productId) {
        requireActive();
        Optional<CartItem> item = items.stream()
                .filter(candidate -> candidate.hasProduct(productId))
                .findFirst();
        item.ifPresent(items::remove);
        return item;
    }

    public void beginCheckout(Instant now) {
        if (status != CartStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only an active cart can begin checkout"
            );
        }

        status = CartStatus.CHECKOUT_IN_PROGRESS;
        updatedAt = now;
    }

    public void completeCheckout(Instant now) {
        if (status != CartStatus.CHECKOUT_IN_PROGRESS) {
            throw new IllegalStateException(
                    "Cart checkout is not in progress"
            );
        }

        status = CartStatus.CHECKED_OUT;
        updatedAt = now;
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

    private CartItem addProduct(UUID productId, int quantity) {
        CartItem item = new CartItem(this, productId, quantity);
        items.add(item);
        return item;
    }

}
